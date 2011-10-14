#!/usr/bin/perl 

use strict;
use warnings;

use Test::More;
use Test::Exception;
require Carp;
use List::Util qw(reduce);

$SIG{__WARN__} = \&Carp::cluck;

my $do_live_tests = $ENV{RELEASE_TESTING};

my($service, $initial_list_count);

unless ($do_live_tests) {
    plan( skip_all => "Acceptance tests for release testing only" );
} else {
    plan( tests => 118 );

my $module = 'Webservice::InterMine';
my $id_file = 't/data/test-identifiers.list';

use_ok($module);

$service = Webservice::InterMine->get_service(
    'squirrel.flymine.org/intermine-test', 'a1v3V1X0f3hdmaybq0l6b7Z4eVG');

$initial_list_count = $service->list_count;

note "\nNo of lists to start with: " . $initial_list_count;

my $query = $service->new_query(class => "Manager");
$query->add_constraint(qw/department.employees.age > 50/);

my $q2 = $service->new_query(class => "Manager");
$q2->add_constraint(qw/age < 37/);

my $first_list = $service->new_list(type => 'Employee', content => 'Helena Fatou "Gareth Keenan"');

my %content_sources = (
    string => 'Alex Anne "Gareth Keenan" Foo Bar "Keith Bishop" Vincent Baz', 
    array => [qw/Brenda Zop Carol Quux Jennifer Delphine Ina/], 
    file => $id_file,
    query => $q2,
);

my %bad_ids = (
    string => 3,
    array => 2,
    file => 1,
    query => 0,
);

my $expected_bad_matches = 3;

# Test Creation

while (my ($source_type, $content) = each %content_sources) {
    my $list;

    lives_ok(
        sub {$list = $service->new_list(
            type => "Employee", content => $content, description => $source_type)},
        "We can make a new list",
    );

    is ($list->size, 5, "... and it has the correct size")
        or diag $list->to_string;

    is (scalar(@{[$list->get_unmatched_ids]}), $bad_ids{$source_type},
        "... and it has the correct number of bad matches")
        or diag $list->to_string, explain [$list->get_unmatched_ids];

}

TEST_ONE_ARG_FORM: {
    my $list;
    lives_ok {$list= $service->new_list($content_sources{query})}
        "Handles one arg form of creation";
    is $list->size, 5, "And it has the right size";
}

# Test Appending

APPENDING: {
    my $list_to_append_to = $service->new_list(
        type => "Employee", content => [qw/Helena/], description => 'appending');
    my $list_to_append_overload = $service->new_list(
        type => "Employee", content => [qw/Helena/], description => 'appending-overload');

    my $appended_size = 1;
    while (my ($source_type, $content) = each %content_sources) {
        $appended_size += 5;

        lives_ok {$list_to_append_to->append($content)} "Can append $source_type";
        is ($list_to_append_to->size, $appended_size, 
            "$list_to_append_to now has the right size after $source_type");

        lives_ok {$list_to_append_overload += $content} "Can use overloading to append $source_type";
        is ($list_to_append_overload->size, $appended_size, 
            "$list_to_append_overload now has the right size after $source_type");
    }

    my $list_to_append_a = $service->new_list(
        type => "Employee", content => [qw/Trudy/], description => 'appendix A');
    my $list_to_append_b = $service->new_list(
        type => "Employee", content => '"Ulf Steinke"', description => 'appendix B');
    my $list_to_append_c = $service->new_list(
        type => "Employee", content => 'Ricky', description => 'appendix B');

    $appended_size += 1;

    lives_ok {$list_to_append_to->append($list_to_append_c)} 
        "Can append a list";
    is($list_to_append_to->size, $appended_size, "List has the right size")
        or diag [$list_to_append_to->get_unmatched_ids];
    lives_ok {$list_to_append_overload += $list_to_append_c}
        "Can overload append a list";
    is($list_to_append_overload->size, $appended_size, "List has the right size");

    $appended_size += 2;

    lives_ok {$list_to_append_to->append([$list_to_append_a, $list_to_append_b])} 
        "Can append multiple lists";
    is($list_to_append_to->size, $appended_size, "List has the right size")
        or diag [$list_to_append_to->get_unmatched_ids];
    lives_ok {$list_to_append_overload += [$list_to_append_a, $list_to_append_b]}
        "Can overload append multiple lists";
    is($list_to_append_overload->size, $appended_size, "List has the right size");
}

my @lists = map {$service->new_list(type => "Employee", content => $_, description => $_)}
                "Lee Alex Ina Brenda Carol Corinne",
                "Lee Brenda Fatou Delphine Karim Emmanuelle",
                "Lee Delphine Carol Helena Anne Malcolm",
                'Lee Helena Ina Jennifer Fatou "Glynn Williams"';

CLONING: {
    my $clone;
    lives_ok {$clone = $service->new_list($lists[0])} "Can clone a list";
    is $clone->size, $lists[0]->size, "And it has the same size";
    isnt $clone->name, $lists[0]->name,"But is has a different name";
}

# Test Unions

UNION: {
    my $union;
    lives_ok {$union = $service->join_lists([@lists])} "Can join a bunch of lists";
    is $union->size, 15, "And the union is of the right size";

    lives_ok {$union = $lists[0] + [@lists[1 .. 3]]} "Can do the same thing with +";
    is $union->size, 15, "And the union is of the right size";

    lives_ok {$union = reduce {$a + $b} @lists} "Can do the same thing with reduce";
    is $union->size, 15, "And the union is of the right size";

    lives_ok {$union = $union + $query} "Can perform union with a query";
    is $union->size, 36, "And it has the right size";

    lives_ok {$union = $q2 + $query} "Can perform union with two queries";
    is $union->size, 23, "And it has the right size";

    lives_ok {$union = $lists[0] | [@lists[1 .. 3]]} "Can do the same thing with |";
    is $union->size, 15, "And the union is of the right size";

    lives_ok {$union = reduce {$a | $b} @lists} "Can do the same thing with reduce";
    is $union->size, 15, "And the union is of the right size";

    lives_ok {$union = $union | $query} "Can perform union with a query";
    is $union->size, 36, "And it has the right size";

    lives_ok {$union = $q2 | $query} "Can perform union with two queries";
    is $union->size, 23, "And it has the right size";
}

# Test Intersecting

INTERSECT: {
    my $intersection;
    lives_ok {$intersection = $service->intersect_lists([@lists], undef, "Global intersection")}
        "Can intersect a bunch of lists.";
    is ($intersection->size, 1, "And it has the right size");

    lives_ok {$intersection = $lists[0] & [@lists[1 .. 3]]}
        "Can do the same thing with overloading";
    is ($intersection->size, 1, "And it has the right size");

    lives_ok {$intersection = reduce {$a & $b} @lists}
        "Can do the same thing with reduction";
    is ($intersection->size, 1, "And it has the right size");

    lives_ok {$intersection = $lists[0] & reduce {$a + $b} @lists[1 .. 3]}
        "Combining intersections and unions";
    is ($intersection->size, 4, "leads to a different result")
        or diag reduce(sub {$a + $b}, @lists[1 .. 3])->size;

    lives_ok { $intersection = $query & reduce {$a + $b} @lists } "Can intersect with a query";
    is $intersection->size, 1, "And it has the right size";

    lives_ok {$intersection = $q2 & $query} "Can perform intersections with two queries";
    is $intersection->size, 4, "And it has the right size";

    my $old_name = $intersection->name;
    lives_ok {$intersection &= $lists[3]} "Can intersection-assign";
    is $intersection->name, $old_name, "The name stays the same";
    is $intersection->size, 0, "But the size has changed";
}

# Test Symmetric Differences

DIFF: {
    my $difference;
    lives_ok {$difference = $service->diff_lists([@lists])} "Can diff lists";
    is $difference->size, 14, "And it has the right size";

    lives_ok {$difference = $lists[0] ^ [@lists[1 .. 3]]} "Can diff lists with ^";
    is $difference->size, 14, "And it has the right size";

    lives_ok {$difference = reduce {$a ^ $b} @lists} "Can diff lists";
    is $difference->size, 8, "BEWARE!! Asymmetric diff is not commutative";

    lives_ok {$difference = $query ^ $q2} "Can diff queries";
    is $difference->size, 19, "And with the right size";

    my $old_name = $difference->name;
    lives_ok {$difference ^= $lists[3]} "Can diff-assign";
    is $difference->name, $old_name, "The name stays the same";
    is $difference->size, 23, "But the size has changed";
}

# Test Subtraction

SUBTR: {
    my $subtraction;
    my $all = $service->new_list(content => $service->new_query(class => 'Employee'));
    lives_ok {$subtraction = $service->subtract_lists([$all], [@lists])}
        "Can subtract lists";
    is $subtraction->size, 117, "And it has the right size"
        or diag $subtraction, reduce {$a + $b} @lists;

    lives_ok {$subtraction = $all - [@lists]}
        "Can subtract lists with -";
    is $subtraction->size, 117, "And it still has the right size"
        or diag $subtraction, reduce {$a + $b} @lists;

    lives_ok {$subtraction = $all - reduce {$a + $b} @lists}
        "Can subtract a single unioned list";
    is $subtraction->size, 117, "And it still has the right size"
        or diag $subtraction, reduce {$a + $b} @lists;

    lives_ok {$subtraction = reduce {$a - $b} $all, @lists}
        "Can use reduction";
    is $subtraction->size, 117, "And it still has the right size"
        or diag $subtraction, reduce {$a + $b} @lists;

    lives_ok {$subtraction = $all - $query}
        "Can subtract queries from lists";
    is $subtraction->size, 110, "Which produces the right size"
        or diag $subtraction, $service->new_list(content => $query);

    lives_ok {$subtraction = $service->new_query(class => "Employee") - $query}
        "Can subtract queries from queries";
    is $subtraction->size, 110, "Which still produces the right size"
        or diag $subtraction, $service->new_list(content => $query);

    lives_ok {$subtraction = $service->join_lists([@lists]) - $all}
        "Can subtract to 0";
    is $subtraction->size, 0, "Subtraction is associative";

    my $old_name = $all->name;
    lives_ok {$all -= $query} "Can subtract-assign";
    is $all->name, $old_name, "The name stays the same";
    is $all->size, 110, "But the size has changed";

    lives_ok {$all -= "Fatou Helena"} "Can coerce arguments from strings";
    is $all->size, 108, "And it works too";
    lives_ok {$all -= [qw/Anne/]} "Can coerce arguments from id lists";
    is $all->size, 107, "And it works too";
    lives_ok {$all -= $id_file} "Can coerce arguments from files";
    is $all->size, 104, "And it works too";
}

SHOWING: {
    my $list = $service->new_list(type => "Employee", content => $id_file);
    my $buffer = '';
    open(my $fh, '>', \$buffer) or die "Horribly, $!";
    $list->show($fh);
    close $fh or die "$!";
    my $name = $list->name;
    my $expected_head = qr!$name \(5 Employees\) \d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\s+Created with Perl API client!;
    my $expected_body = q!-------------+--------------+-------------------+--------------
Employee.age.|.Employee.end.|.Employee.fullTime.|.Employee.name
-------------+--------------+-------------------+--------------
30...........|.6............|.false.............|.Karim........
33...........|.1............|.false.............|.Jennifer.Schirrmann
58...........|.6............|.true..............|.Jean-Marc....
62...........|.UNDEF........|.false.............|.David.Brent..
68...........|.UNDEF........|.false.............|.Frank.Möllers
!;
    my ($head, $body) = split(/\n/, $buffer, 2);    
    like $head, $expected_head, "Can produce a head for a summary";

    # Make spaces visible for diagnostics
    $body =~ s/ /./g;
    is $body, $expected_body,  "Can show a summary of a list";
}

PRINTING: {
    my $list = $service->new_list(type => "Employee", content => $id_file);
    my $buffer = '';
    open(my $fh, '>', \$buffer) or die "Horribly, $!";
    $list->print_results(to => $fh, columnheaders => 1);
    close $fh or die "$!";
    my $expected = qq|Employee > Years Alive\tEmployee > End\tEmployee > Works Full Time\tEmployee > Name
30\t6\tfalse\tKarim
33\t1\tfalse\tJennifer Schirrmann
58\t6\ttrue\tJean-Marc
62\t""\tfalse\tDavid Brent
68\t""\tfalse\tFrank Möllers
|;
    is $buffer, $expected, "Can print a list";
}

RENAME_DELETE: {
    my $list = $service->new_list(type => "Employee", content => $id_file);

    $list->rename("my_renamed_list");
    is $list->name, "my_renamed_list", "Can rename an object";
    my $from_service = $service->list("my_renamed_list");

    is $list->to_string, $from_service->to_string, "Renaming a list updates on the server";
    
    my $list_count = $service->list_count;
    $list->delete;
    is $service->list_count, $list_count - 1, "Deleting a list is reflected in the list count";
    is $service->list("my_renamed_list"), undef, "Deleting a list makes it inaccessible through the service object";

    dies_ok {$list += "Corinne"} "Attempts to use the list throw exceptions";
}

note "\nNo of lists made: " . ($service->list_count - $initial_list_count);
}

END {
    if ($service) {
        is $service->list_count, $initial_list_count, "The service cleaned up after itself.";
    }
}

__END__

