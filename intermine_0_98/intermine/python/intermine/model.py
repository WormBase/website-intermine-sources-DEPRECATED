from xml.dom import minidom
import weakref
import re

from intermine.util import openAnything, ReadableException
from intermine.lists.list import List

"""
Classes representing the data model
===================================

Representations of tables and columns, and behaviour
for validating connections between them.

"""

__author__ = "Alex Kalderimis"
__organization__ = "InterMine"
__license__ = "LGPL"
__contact__ = "dev@intermine.org"



class Class(object):
    """
    An abstraction of database tables in the data model
    ===================================================

    These objects refer to the table objects in the
    InterMine ORM layer.

    SYNOPSIS
    --------

    >>>  service = Service("http://www.flymine.org/query/service")
    >>>  model = service.model
    >>>
    >>>  if "Gene" in model.classes:
    ...      gene_cd = model.get_class("Gene")
    ...      print "Gene has", len(gene_cd.fields), "fields"
    ...      for field in gene_cd.fields:
    ...          print " - ", field.name

    OVERVIEW
    --------

    Each class can have attributes (columns) of various types,
    and can have references to other classes (tables), on either
    a one-to-one (references) or one-to-many (collections) basis

    Classes should not be instantiated by hand, but rather used
    as part of the model they belong to.

    """
    def __init__(self, name, parents, model):
        """
        Constructor - Creates a new Class descriptor
        ============================================

        >>> cd = intermine.model.Class("Gene", ["SequenceFeature"])
        <intermine.model.Class: Gene>

        This constructor is called when deserialising the 
        model - you should have no need to create Classes by hand

        @param name: The name of this class
        @param parents: a list of parental names

        """
        self.name = name
        self.parents = parents
        self.model = model
        self.parent_classes = []
        self.field_dict = {}
        id = Attribute("id", "Integer", self) # All classes have the id attr
        self.field_dict["id"] = id

    def __repr__(self): 
        return '<' + self.__module__ + "." + self.__class__.__name__ + ': ' + self.name + '>'

    @property
    def fields(self):
        """
        The fields of this class
        ========================

        The fields are returned sorted by name. Fields
        includes all Attributes, References and Collections

        @rtype: list(L{Field})
        """
        return sorted(self.field_dict.values(), key=lambda field: field.name)

    @property
    def attributes(self):
        """
        The fields of this class which contain data
        ===========================================

        @rtype: list(L{Attribute})
        """
        return filter(lambda x: isinstance(x, Attribute), self.fields)

    @property
    def references(self):
        """
        fields which reference other objects
        ====================================

        @rtype: list(L{Reference})
        """
        def isRef(x): return isinstance(x, Reference) and not isinstance(x, Collection)
        return filter(isRef, self.fields)

    @property
    def collections(self):
        """
        fields which reference many other objects
        =========================================

        @rtype: list(L{Collection})
        """
        return filter(lambda x: isinstance(x, Collection), self.fields)

    def get_field(self, name):
        """
        Get a field by name
        ===================

        The standard way of retrieving a field

        @raise ModelError: if the Class does not have such a field

        @rtype: subclass of L{intermine.model.Field}
        """
        if name in self.field_dict:
            return self.field_dict[name]
        else:
            raise ModelError("There is no field called %s in %s" % (name, self.name))

    def isa(self, other):
        """
        Check if self is, or inherits from other
        ========================================

        This method validates statements about inheritance. 
        Returns true if the "other" is, or is within the 
        ancestry of, this class

        Other can be passed as a name (str), or as the class object itself

        @rtype: boolean
        """
        if isinstance(other, Class):
            other_name = other.name
        else:
            other_name = other
        if self.name == other_name:
            return True
        if other_name in self.parents:
            return True
        for p in self.parent_classes:
            if p.isa(other):
                return True
        return False
    

class Field(object):
    """
    A class representing columns on database tables
    ===============================================

    The base class for attributes, references and collections. All
    columns in DB tables are represented by fields

    SYNOPSIS
    --------

        >>> service = Service("http://www.flymine.org/query/service")
        >>> model = service.model
        >>> cd = model.get_class("Gene")
        >>> print "Gene has", len(cd.fields), "fields"
        >>> for field in gene_cd.fields:
        ...        print " - ", field
        Gene has 45 fields
            -  CDSs is a group of CDS objects, which link back to this as gene
            -  GLEANRsymbol is a String
            -  UTRs is a group of UTR objects, which link back to this as gene
            -  alleles is a group of Allele objects, which link back to this as gene
            -  chromosome is a Chromosome
            -  chromosomeLocation is a Location
            -  clones is a group of CDNAClone objects, which link back to this as gene
            -  crossReferences is a group of CrossReference objects, which link back to this as subject
            -  cytoLocation is a String
            -  dataSets is a group of DataSet objects, which link back to this as bioEntities
            -  downstreamIntergenicRegion is a IntergenicRegion
            -  exons is a group of Exon objects, which link back to this as gene
            -  flankingRegions is a group of GeneFlankingRegion objects, which link back to this as gene
            -  goAnnotation is a group of GOAnnotation objects
            -  homologues is a group of Homologue objects, which link back to this as gene
            -  id is a Integer
            -  interactions is a group of Interaction objects, which link back to this as gene
            -  length is a Integer
            ...
      
    @see: L{Attribute} 
    @see: L{Reference}
    @see: L{Collection}
    """
    def __init__(self, name, type_name, class_origin):
        """
        Constructor - DO NOT USE
        ========================

        THIS CLASS IS NOT MEANT TO BE INSTANTIATED DIRECTLY
            
        you are unlikely to need to do 
        so anyway: it is recommended you access fields
        through the classes generated by the model

        @param name: The name of the reference
        @param type_name: The name of the model.Class this refers to
        @param class_origin: The model.Class this was declared in

        """
        self.name = name
        self.type_name = type_name
        self.type_class = None
        self.declared_in = class_origin
    def toString(self):
        return self.name + " is a " + self.type_name
    def __str__(self):
        return self.toString()


class Attribute(Field):
    """
    Attributes represent columns that contain actual data
    =====================================================

    The Attribute class inherits all the behaviour of L{intermine.model.Field}
    """
    pass

class Reference(Field):
    """
    References represent columns that refer to records in other tables
    ==================================================================
    
    In addition the the behaviour and properties of Field, references
    may also have a reverse reference, if the other record points 
    back to this one as well. And all references will have their
    type upgraded to a type_class during parsing
    """
    def __init__(self, name, type_name, class_origin, reverse_ref=None):
        """
        Constructor
        ===========

        In addition to the a parameters of Field, Reference also 
        takes an optional reverse reference name (str)

        @param name: The name of the reference
        @param type_name: The name of the model.Class this refers to
        @param class_origin: The model.Class this was declared in
        @param reverse_ref: The name of the reverse reference (default: None)

        """
        self.reverse_reference_name = reverse_ref
        super(Reference, self).__init__(name, type_name, class_origin)
        self.reverse_reference = None
    def toString(self):
        """
        Return a string representation
        ==============================

        @rtype: str
        """
        s = super(Reference, self).toString()
        if self.reverse_reference is None:
            return s
        else:
            return s + ", which links back to this as " + self.reverse_reference.name

class Collection(Reference):
    """
    Collections are references which refer to groups of objects
    ===========================================================

    Collections have all the same behaviour and properties as References
    """
    def toString(self):
        """Return a string representation"""
        ret = super(Collection, self).toString().replace(" is a ", " is a group of ")
        if self.reverse_reference is None:
            return ret + " objects"
        else:
            return ret.replace(", which links", " objects, which link")
        

class Path(object):
    """
    A class representing a validated dotted string path
    ===================================================

    A path represents a connection between records and fields

    SYNOPSIS
    --------

        >>> service = Service("http://www.flymine.org/query/service")
            model = service.model
            path = model.make_path("Gene.organism.name")
            path.is_attribute()
        ... True
        >>> path2 = model.make_path("Gene.proteins")
            path2.is_attribute()
        ... False
        >>> path2.is_reference()
        ... True
        >>> path2.get_class()
        ... <intermine.model.Class: gene>

    OVERVIEW
    --------

    This class is used for performing validation on dotted path strings. 
    The simple act of parsing it into existence will validate the path
    to some extent, but there are additional methods for verifying certain
    relationships as well
    """
    def __init__(self, path, model, subclasses={}):
        """
        Constructor
        ===========

          >>> path = Path("Gene.name", model)

        You will not need to use this constructor directly. Instead,
        use the "make_path" method on the model to construct paths for you.
        
        @param path: the dotted path string (eg: Gene.proteins.name)
        @type path: str
        @param model: the model to validate the path against
        @type model: L{Model}
        @param subclasses: a dict which maps subclasses (defaults to an empty dict)
        @type subclasses: dict
        """
        self.model = weakref.proxy(model)
        if isinstance(path, Class):
            self._string = path.name
            self.parts = [path]
        else:
            self._string = str(path)
            self.parts = model.parse_path_string(str(path), subclasses)

    def __str__(self):
        return self._string

    def __repr__(self):
        return '<' + self.__module__ + "." + self.__class__.__name__ + ": " + self._string + '>'

    @property
    def root(self):
        """
        The descriptor for the first part of the string. This should always a class descriptor.

        @rtype: L{intermine.model.Class}
        """
        return self.parts[0]

    @property
    def end(self):
        """
        The descriptor for the last part of the string.
        
        @rtype: L{model.Class} or L{model.Field}
        """
        return self.parts[-1]

    def get_class(self):
        """
        Return the class object for this path, if it refers to a class
        or a reference. Attribute paths return None
        
        @rtype: L{model.Class}
        """
        if self.is_class():
            return self.end
        elif self.is_reference():
            return self.end.type_class
        else:
            return None
    end_class = property(get_class)
    
    def is_reference(self):
        """
        Return true if the path is a reference, eg: Gene.organism or Gene.proteins
        Note: Collections are ALSO references
        
        @rtype: boolean
        """
        return isinstance(self.end, Reference)

    def is_class(self):
        """
        Return true if the path just refers to a class, eg: Gene
        
        @rtype: boolean
        """
        return isinstance(self.end, Class)

    def is_attribute(self):
        """
        Return true if the path refers to an attribute, eg: Gene.length
        
        @rtype: boolean
        """
        return isinstance(self.end, Attribute)

class Column(object):

    def __init__(self, path, model, subclasses={}, query=None):
        self._model = model
        self._query = query
        self._subclasses = subclasses
        if isinstance(path, Path):
            self._path = path
        else:
            self._path = model.make_path(path, subclasses)

    def select(self, *cols):
        q = self._model.service.new_query(str(self))
        q.select(*cols)
        return q

    def __getattr__(self, name):
        cld = self._path.get_class()
        if cld is not None:
            try:
                fld = cld.get_field(name)
                return Column(str(self) + "." + name, self._model, self._subclasses, self._query)
            except ModelError, e:
                raise AttributeError(str(e))
        raise AttributeError("No attribute '" + name + "'") 

    def __str__(self):
        return str(self._path)

    def __mod__(self, other):
        if isinstance(other, tuple):
            l = [str(self), "LOOKUP"]
            l.extend(other)
            return tuple(l) 
        else:
            return (str(self), 'LOOKUP', str(other))

    def __rshift__(self, other):
        return (str(self), str(other))

    def __eq__(self, other):
        if isinstance(other, Column):
            return (str(self), "IS", str(other))
        elif other is None:
            return (str(self), "IS NULL")
        elif isinstance(other, list):
            return (str(self), "ONE OF", other)
        elif isinstance(other, List):
            return (str(self), "IN", other.name)
        else:
            return (str(self), "=", other)

    def __ne__(self, other):
        if isinstance(other, Column):
            return (str(self), "IS NOT", str(other))
        elif other is None:
            return (str(self), "IS NOT NULL")
        elif isinstance(other, list):
            return (str(self), "NONE OF", other)
        elif isinstance(other, List):
            return (str(self), "NOT IN", other.name)
        else:
            return (str(self), "!=", other)

    def __lt__(self, other):
        return (str(self), "<", other)

    def __le__(self, other):
        return (str(self), "<=", other)

    def __gt__(self, other):
        return (str(self), ">", other)

    def __ge__(self, other):
        return (str(self), ">=", other)

class Model(object):
    """
    A class for representing the data model of an InterMine datawarehouse
    =====================================================================

    An abstraction of the database schema

    SYNOPSIS
    --------

        >>> service = Service("http://www.flymine.org/query/service")
        >>> model = service.model
        >>> model.get_class("Gene")
        <intermine.model.Class: Gene>

    OVERVIEW
    --------

    This class represents the data model  - ie. an abstraction
    of the database schema. It can be used to introspect what 
    data is available and how it is inter-related
    """
    def __init__(self, source, service=None):
        """
        Constructor
        ===========

          >>> model = Model(xml)

        You will most like not need to create a model directly, 
        instead get one from the Service object:
        
        @see: L{intermine.webservice.Service}

        @param source: the model.xml, as a local file, string, or url
        """
        assert source is not None
        self.source = source
        if service is not None:
            self.service = weakref.proxy(service) 
        else:
            self.service = service
        self.classes= {}
        self.parse_model(source)
        self.vivify()

        # Make sugary aliases
        self.table = self.column

    def parse_model(self, source):
        """
        Create classes, attributes, references and collections from the model.xml
        =========================================================================

        The xml can be provided as a file, url or string. This method
        is called during instantiation - it does not need to be called 
        directly.

        @param source:  the model.xml, as a local file, string, or url
        @raise ModelParseError: if there is a problem parsing the source
        """
        try:
            io = openAnything(source)
            doc = minidom.parse(io)
            for node in doc.getElementsByTagName('model'):
                self.name = node.getAttribute('name')
                self.package_name = node.getAttribute('package')
                assert node.nextSibling is None, "More than one model element"
                assert self.name and self.package_name, "No model name or package name"
     
            for c in doc.getElementsByTagName('class'):
                class_name = c.getAttribute('name')
                assert class_name, "Name not defined in" + c.toxml()
                def strip_java_prefix(x): 
                    return re.sub(r'.*\.', '', x)
                parents = map(strip_java_prefix, 
                        c.getAttribute('extends').split(' '))
                cl =  Class(class_name, parents, self)
                for a in c.getElementsByTagName('attribute'):
                    name = a.getAttribute('name')
                    type_name = strip_java_prefix(a.getAttribute('type'))
                    at = Attribute(name, type_name, cl)
                    cl.field_dict[name] = at
                for r in c.getElementsByTagName('reference'):
                    name = r.getAttribute('name')
                    type_name = r.getAttribute('referenced-type')
                    linked_field_name = r.getAttribute('reverse-reference')
                    ref = Reference(name, type_name, cl, linked_field_name)
                    cl.field_dict[name] = ref
                for co in c.getElementsByTagName('collection'):
                    name = co.getAttribute('name')
                    type_name = co.getAttribute('referenced-type')
                    linked_field_name = co.getAttribute('reverse-reference')
                    col = Collection(name, type_name, cl, linked_field_name)
                    cl.field_dict[name] = col
                self.classes[class_name] = cl
        except Exception, error:
            raise ModelParseError("Error parsing model", source, error)

    def vivify(self):
        """
        Make names point to instances and insert inherited fields
        =========================================================
        
        This method ensures the model is internally consistent. This method
        is called during instantiaton. It does not need to be called
        directly.

        @raise ModelError: if the names point to non-existent objects
        """
        for c in self.classes.values():
            c.parent_classes = self.to_ancestry(c)
            for pc in c.parent_classes:
                c.field_dict.update(pc.field_dict)
            for f in c.fields:
                f.type_class = self.classes.get(f.type_name)
                if hasattr(f, 'reverse_reference_name') and f.reverse_reference_name != '':
                    rrn = f.reverse_reference_name
                    f.reverse_reference = f.type_class.field_dict[rrn]

    def to_ancestry(self, cd):
        """
        Returns the lineage of the class
        ================================
        
            >>> classes = Model.to_ancestry(cd)

        Returns the class' parents, and all the class' parents' parents

        @rtype: list(L{intermine.model.Class})
        """
        parents = cd.parents
        def defined(x): return x is not None # weeds out the java classes
        def to_class(x): return self.classes.get(x)
        ancestry = filter(defined, map(to_class, parents))
        for ancestor in ancestry:
            ancestry.extend(self.to_ancestry(ancestor))
        return ancestry

    def to_classes(self, classnames):
        """
        take a list of class names and return a list of classes
        =======================================================

            >>> classes = model.to_classes(["Gene", "Protein", "Organism"])

        This simply maps from a list of strings to a list of 
        classes in the calling model.

        @raise ModelError: if the list of class names includes ones that don't exist

        @rtype: list(L{intermine.model.Class})
        """
        return map(self.get_class, classnames)

    def column(self, path, *rest):
        return Column(path, self, *rest)

    def __getattr__(self, name):
        return self.column(name)

    def get_class(self, name):
        """
        Get a class by its name, or by a dotted path
        ============================================

            >>> model = Model("http://www.flymine.org/query/service/model")
            >>> model.get_class("Gene")
            <intermine.model.Class: Gene>
            >>> model.get_class("Gene.proteins")
            <intermine.model.Class: Protein>

        This is the recommended way of retrieving a class from
        the model. As well as handling class names, you can also
        pass in a path such as "Gene.proteins" and get the 
        corresponding class back (<intermine.model.Class: Protein>)

        @raise ModelError: if the class name refers to a non-existant object

        @rtype: L{intermine.model.Class}
        """
        if name.find(".") != -1:
            path = self.make_path(name)
            if path.is_attribute():
                raise ModelError("'" + str(path) + "' is not a class")
            else:
                return path.get_class()
        if name in self.classes:
          return self.classes[name]
        else:
          raise ModelError("'" + name + "' is not a class in this model")

    def make_path(self, path, subclasses={}):
        """
        Return a path object for the given path string
        ==============================================

            >>> path = Model.make_path("Gene.organism.name")
            <intermine.model.Path: Gene.organism.name>

        This is recommended manner of constructing path objects.

        @type path: str
        @type subclasses: dict

        @raise PathParseError: if there is a problem parsing the path string

        @rtype: L{intermine.model.Path}
        """
        return Path(path, self, subclasses)

    def validate_path(self, path_string, subclasses={}):
        """
        Validate a path
        ===============

            >>> try:
            ...     model.validate_path("Gene.symbol")
            ...     return "path is valid"
            ... except PathParseError:
            ...     return "path is invalid"
            "path is valid"

        When you don't need to interrogate relationships
        between paths, simply using this method to validate
        a path string is enough. It guarantees that there
        is a descriptor for each section of the string, 
        with the appropriate relationships

        @raise PathParseError: if there is a problem parsing the path string
        """
        try:
            self.parse_path_string(path_string, subclasses)
            return True
        except PathParseError, e:
            raise PathParseError("Error parsing '%s' (subclasses: %s)" 
                            % ( path_string, str(subclasses) ), e )

    def parse_path_string(self, path_string, subclasses={}):
        """
        Parse a path string into a list of descriptors - one for each section
        =====================================================================
    
            >>> parts = Model.parse_path_string(string)

        This method is used when making paths from a model, and 
        when validating path strings. It probably won't need to 
        be called directly.

        @see: L{intermine.model.Model.make_path}
        @see: L{intermine.model.Model.validate_path}
        @see: L{intermine.model.Path}
        """
        descriptors = []
        names = path_string.split('.')
        root_name = names.pop(0)
     
        root_descriptor = self.get_class(root_name)
        descriptors.append(root_descriptor)
     
        if root_name in subclasses:
            current_class = self.get_class(subclasses[root_name])
        else:
            current_class = root_descriptor 
     
        for field_name in names:
            field = current_class.get_field(field_name)
            descriptors.append(field)
     
            if isinstance(field, Reference):
                key = '.'.join(map(lambda x: x.name, descriptors))
                if key in subclasses:
                    current_class = self.get_class(subclasses[key])
                else: 
                    current_class = field.type_class
            else:
                current_class = None
     
        return descriptors 

class ModelError(ReadableException):
    pass

class PathParseError(ModelError):
    pass

class ModelParseError(ModelError):
   
    def __init__(self, message, source, cause=None):
        self.source = source
        super(ModelParseError, self).__init__(message, cause)

    def __str__(self):
        base = repr(self.message) + ":" + repr(self.source)
        if self.cause is None:
            return base
        else:
            return base + repr(self.cause)

