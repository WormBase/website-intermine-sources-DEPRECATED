package Webservice::InterMine::Query::Roles::Runnable;

use MooseX::Role::WithOverloading;
requires qw(view service model get_request_parameters resource_path
            upload_path to_xml validate);

use MooseX::Types::Moose qw(Str);
use Perl6::Junction qw/any/;
use Webservice::InterMine::Service;
use Webservice::InterMine::Types qw(ResultIterator);
use Webservice::InterMine::ResultObject;
use IO::Handle;

use constant {
    DEFAULT_FORMAT => 'tab',
};

use overload (
    '<>' => 'next_result',
    fallback => 1,
);

around BUILDARGS => sub {
    my $orig  = shift;
    my $class = shift;
    if ( @_ == 1 && !ref $_[0] ) {
        my $service = Webservice::InterMine::Service->new( root => $_[0] );
        my $model = $service->model;
        return $class->$orig(
            service => $service,
            model   => $model,
        );
    }
    else {
        return $class->$orig(@_);
    }
};

# In Core queries, Paths are made with reference to the 
# query rather than a service.
around path => sub {
    my $orig = shift;
    my ($self, $str) = @_;
    my $path = $self->$orig($str);
    $path->{service} = $self->service;
    return $path;
};

=head1 NAME 

Webservice::InterMine::Query::Roles::Runnable - Composable behaviour for runnable queries

=head1 DESCRIPTION 

This module provides composable behaviour for running a query against a webservice and getting the results.

=head1 METHODS

=head2 results_iterator 

Returns a results iterator for use with a query.

The following options are available:

=over 4

=item * as => $format

Possible values: (string|tsv|csv|arrayrefs|hashrefs|jsonobjects|jsonrows|count)

The format to request results in. The default is C<arrayrefs>

=item * size => $size

The number of results to return. Leave undefined for "all" (default).

=item * start => $start 

The first result to return (starting at 0). The default is 0.

=item * columnheaders => 0/1/friendly/path

Whether to return the column headers at the top of TSV/CSV results. The default is
false. There are two styles - friendly: "Gene > pathways > name" and 
path: "Gene.pathways.name". The default style is friendly if a true value is entered and
it is not "path".

=item * json => $json_processor

Possible values: (inflate|instantiate|raw|perl)

What to do with JSON results. The results can be returned as inflated objects,
full instantiated Moose objects, a raw json string, or as a perl
data structure. (default is C<perl>).

=back

=cut

has _iterator => (
    is => 'ro',
    isa => ResultIterator,
    lazy_build => 1,
    builder => 'results_iterator',
);

sub next_result {
    my $self = shift;
    my $next = $self->_iterator->next;
    $self->_clear_iterator unless (defined $next);
    return $next;
}

sub results_iterator {
    my $self  = shift;
    my %args  = @_;

    $self->validate;

    my $row_format  = delete($args{as})   || "rr";
    $row_format = 'tab' if ($row_format eq 'string' || $row_format eq 'tsv');
    my $json_format = delete($args{json}) || "perl";
    my $roles       = delete $args{with};

    my %query_form = $self->get_request_parameters;
    
    # Set optional parameters
    for my $opt (qw/start size columnheaders/) {
        $query_form{$opt} = $args{$opt} if (defined $args{$opt});
    }
    warn join(', ', map {"$_ => $query_form{$_}"} keys %query_form) if $ENV{DEBUG};
    return $self->service->get_results_iterator(
        $self->url,
        \%query_form,
        $self->view, 
        $row_format, 
        $json_format,
        $roles, 
    );
}

sub iterator {
    goto &results_iterator;
}

=head2 results( %options )

returns the results from a query in the result format
specified. 

The following options are available:

=over 4

=item * as => $format

Possible values: (tsv|csv|arrayrefs|hashrefs|jsonobjects|jsonrows|count)

The format to request results in. The default is C<arrayrefs>

=item * size => $size

The number of results to return. Leave undefined for "all" (default).

=item * start => $start 

The first result to return (starting at 0). The default is 0.

=item * addheaders => 0/1/friendly/path

Whether to return the column headers at the top of TSV/CSV results. The default is
false. There are two styles - friendly: "Gene > pathways > name" and 
path: "Gene.pathways.name". The default style is friendly if a true value is entered and
it is not "path".

=item * json => $json_processor

Possible values: (inflate|instantiate|perl)

What to do with JSON results. The results can be returned as inflated objects,
full instantiated Moose objects, a raw json string, or as a perl
data structure. (default is C<perl>).

=back

=cut

sub results {
    my $self = shift;
    my $iter = $self->results_iterator(@_);
    return $iter->get_all();
}

=head2 all(%options)

Return all rows of results.
This method takes the same options as C<results>, but any start and size 
arguments given are ignored. Note that the server code limits result-sets
to 10,000,000 rows in size, no matter what.

=cut

sub all { 
    my ($self, %options) = @_;
    $options{start} = 0;
    delete($options{size});
    return $self->results(%options);
}

=head2 first(%options)

Return the first result (row or object).
This method takes the same options as C<results>, but any size 
arguments given are ignored. May return C<undef> if there
are no results.

=cut

sub first {
    my ($self, %options) = @_;
    $options{start} ||= 0;
    # rows and objects are not the same thing
    $options{size} = ($options{as} and $options{as} eq 'jsonobjects') ? undef : 1;
    my $it = $self->results_iterator(%options);
    return $it->next;
}

=head2 one(%options)

Return one result (row or result object), throwing an error if more than one is received.

=cut

sub one {
    my ($self, %options) = @_;
    my $it = $self->results_iterator(%options);
    my $one = $it->next;
    confess "No results received" unless $one;
    confess "More than one result received" if $it->next;
    return $one;
}

=head2 get_count

A convenience method that returns the number of result rows a query
returns.

=cut

sub get_count {
    my $self = shift;
    my $iter = $self->results_iterator(as => "count");
    return join('', $iter->get_all());
}

=head2 count

Alias for get_count

=cut

sub count {
    goto &get_count;
}

=head2 url 

Get the url for a webservice resource.

=cut

sub url {
    my $self = shift;
    my $url = $self->service->root . $self->resource_path;
    my $uri = URI->new($url);
    return $uri;
}

=head2 get_upload_url

get the url to use to upload queries to the webservice.

=cut

sub get_upload_url {
    my $self = shift;
    my $url = return $self->service->root . $self->upload_path;
    my $uri = URI->new($url);
    return $uri;
}

=head2 save

Save this query in the user's history in the connected webservice. For queries
this will be saved into query history, and templates will be saved into your 
personal collection of private templates.

=cut

sub save {
    my $self = shift;
    my %args = @_;
    $self->validate;
    $self->name( $args{name} ) if ( exists $args{name} );
    my $xml  = $self->to_xml;
    my $url  = $self->get_upload_url;
    my $resp = $self->service->send_off( $xml => $url );
    my $name = $self->name;
    my $root = $self->service->root;
    unless ($resp =~ /$name\tsuccess/i) {
        confess "Error saving $name to your account at $root:\n$resp";
    }
    return;
}

1;

=head1 SEE ALSO

=over 4

=item * L<Webservice::InterMine::Cookbook> for guide on how to use these modules.

=item * L<Webservice::InterMine::Query>

=item * L<Webservice::InterMine::Service>

=item * L<Webservice::InterMine::Query::Template>

=back

=head1 AUTHOR

Alex Kalderimis C<< <dev@intermine.org> >>

=head1 BUGS

Please report any bugs or feature requests to C<dev@intermine.org>.

=head1 SUPPORT

You can find documentation for this module with the perldoc command.

    perldoc Webservice::InterMine::Query::Roles::Runnable

You can also look for information at:

=over 4

=item * Webservice::InterMine

L<http://www.intermine.org>

=item * Documentation

L<http://www.intermine.org/perlapi>

=back

=head1 COPYRIGHT AND LICENSE

Copyright 2006 - 2011 FlyMine, all rights reserved.

This program is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=cut
