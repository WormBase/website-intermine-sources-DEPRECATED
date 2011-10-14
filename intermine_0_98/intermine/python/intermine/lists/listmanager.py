import weakref

# Use core json for 2.6+, simplejson for <=2.5
try:
    import json
except ImportError:
    import simplejson as json

import urllib
import codecs

from intermine.lists.list import List

class ListManager(object):
    """
    A Class for Managing List Content and Operations
    ================================================

    This class serves as a delegate for the intermine.webservice.Service class, 
    managing list content and operations. 

    This class is not meant to be called itself, but rather for its
    methods to be called by the service object.
    """

    DEFAULT_LIST_NAME = "my_list_"
    DEFAULT_DESCRIPTION = "List created with Python client library"

    INTERSECTION_PATH = '/lists/intersect/json'
    UNION_PATH = '/lists/union/json'
    DIFFERENCE_PATH = '/lists/diff/json'
    SUBTRACTION_PATH = '/lists/subtract/json'

    def __init__(self, service):
        self.service = weakref.proxy(service)
        self.lists = None
        self._temp_lists = set()

    def refresh_lists(self):
        """Update the list information with the latest details from the server"""
        self.lists = {}
        url = self.service.root + self.service.LIST_PATH
        sock = self.service.opener.open(url)
        data = sock.read()
        sock.close()
        list_info = json.loads(data)
        if not list_info.get("wasSuccessful"):
            raise ListServiceError(list_info.get("error"))
        for l in list_info["lists"]:
            l = ListManager.safe_dict(l) # Workaround for python 2.6 unicode key issues
            self.lists[l["name"]] = List(service=self.service, manager=self, **l)

    @staticmethod
    def safe_dict(d):
        """Recursively clone json structure with UTF-8 dictionary keys""" 
        if isinstance(d, dict): 
            return dict([(k.encode('utf-8'), v) for k,v in d.iteritems()]) 
        else: 
            return d 

    def get_list(self, name):
        """Return a list from the service by name, if it exists"""
        if self.lists is None:
            self.refresh_lists()
        return self.lists.get(name)

    def l(self, name):
        """Alias for get_list"""
        return self.get_list(name)

    def get_all_lists(self):
        """Get all the lists on a webservice"""
        if self.lists is None:
            self.refresh_lists()
        return self.lists.values()

    def get_all_list_names(self):
        """Get all the names of the lists in a particular webservice"""
        if self.lists is None:
            self.refresh_lists()
        return self.lists.keys()

    def get_list_count(self):
        """
        Return the number of lists accessible at the given webservice. 
        This number will vary depending on who you are authenticated as.
        """
        return len(self.get_all_list_names())

    def get_unused_list_name(self):
        """Get a list name that does not conflict with any lists you have access to"""
        list_names = self.get_all_list_names()
        counter = 1
        name = self.DEFAULT_LIST_NAME + str(counter)
        while name in list_names:
            counter += 1
            name = self.DEFAULT_LIST_NAME + str(counter)
        self._temp_lists.add(name)
        return name

    def create_list(self, content, list_type="", name=None, description=None, tags=[]):
        """
        Create a new list in the webservice
        ===================================

        If no name is given, the list will be considered to be a temporary
        list, and will be automatically deleted when the program ends. To prevent
        this happening, give the list a name, either on creation, or by renaming it.

        @rtype: intermine.lists.List
        """
        if description is None:
            description = self.DEFAULT_DESCRIPTION

        if name is None:
            name = self.get_unused_list_name()

        try:
            ids = open(content).read()
        except (TypeError, IOError):
            if isinstance(content, basestring):
                ids = content
            else:
                try:
                    ids = "\n".join(map(lambda x: '"' + x + '"', iter(content)))
                except TypeError:
                    try:
                        uri = content.get_list_upload_uri()
                    except:
                        content = content.to_query()
                        uri = content.get_list_upload_uri()
                    params = content.to_query_params()
                    params["listName"] = name
                    params["description"] = description
                    form = urllib.urlencode(params)
                    resp = self.service.opener.open(uri, form)
                    data = resp.read()
                    resp.close()
                    return self.parse_list_upload_response(data) 

        uri = self.service.root + self.service.LIST_CREATION_PATH
        query_form = {'name': name, 'type': list_type, 'description': description, 'tags': ";".join(tags)}
        uri += "?" + urllib.urlencode(query_form)
        data = self.service.opener.post_plain_text(uri, ids)
        return self.parse_list_upload_response(data)

    def parse_list_upload_response(self, response):
        """
        Intepret the response from the webserver to a list request, and return the List it describes
        """
        try:
            response_data = json.loads(response)
        except ValueError:
            raise ListServiceError("Error parsing response: " + response)
        if not response_data.get("wasSuccessful"):
            raise ListServiceError(response_data.get("error"))
        self.refresh_lists()
        new_list = self.get_list(response_data["listName"])
        failed_matches = response_data.get("unmatchedIdentifiers")
        new_list._add_failed_matches(failed_matches)
        return new_list

    def delete_lists(self, lists):
        """Delete the given lists from the webserver"""
        for l in lists:
            if isinstance(l, List):
                name = l.name
            else:
                name = str(l)
            if name not in self.get_all_list_names():
                continue
            uri = self.service.root + self.service.LIST_PATH
            query_form = {'name': name}
            uri += "?" + urllib.urlencode(query_form)
            response = self.service.opener.delete(uri)
            response_data = json.loads(response)
            if not response_data.get("wasSuccessful"):
                raise ListServiceError(response_data.get("error"))
        self.refresh_lists()

    def delete_temporary_lists(self):
        """Delete all the lists considered temporary (those created without names)"""
        self.delete_lists(self._temp_lists)
        self._temp_lists = set()

    def intersect(self, lists, name=None, description=None, tags=[]):
        """Calculate the intersection of a given set of lists, and return the list representing the result"""
        return self._do_operation(self.INTERSECTION_PATH, "Intersection", lists, name, description, tags)

    def union(self, lists, name=None, description=None, tags=[]):
        """Calculate the union of a given set of lists, and return the list representing the result"""
        return self._do_operation(self.UNION_PATH, "Union", lists, name, description, tags)

    def xor(self, lists, name=None, description=None, tags=[]):
        """Calculate the symmetric difference of a given set of lists, and return the list representing the result"""
        return self._do_operation(self.DIFFERENCE_PATH, "Difference", lists, name, description, tags)

    def subtract(self, lefts, rights, name=None, description=None, tags=[]):
        """Calculate the subtraction of rights from lefts, and return the list representing the result"""
        left_names = self.make_list_names(lefts)
        right_names = self.make_list_names(rights)
        if description is None:
            description = "Subtraction of " + ' and '.join(right_names) + " from " + ' and '.join(left_names)
        if name is None:
            name = self.get_unused_list_name()
        uri = self.service.root + self.SUBTRACTION_PATH
        uri += '?' + urllib.urlencode({
            "name": name,
            "description": description,
            "references": ';'.join(left_names),
            "subtract": ';'.join(right_names),
            "tags": ";".join(tags)
            })
        resp = self.service.opener.open(uri)
        data = resp.read()
        resp.close()
        return self.parse_list_upload_response(data)

    def _do_operation(self, path, operation, lists, name, description, tags):
        list_names = self.make_list_names(lists)
        if description is None:
            description = operation + " of " + ' and '.join(list_names)
        if name is None:
            name = self.get_unused_list_name()
        uri = self.service.root + path
        uri += '?' + urllib.urlencode({
            "name": name,
            "lists": ';'.join(list_names),
            "description": description,
            "tags": ";".join(tags)
            })
        resp = self.service.opener.open(uri)
        data = resp.read()
        resp.close()
        return self.parse_list_upload_response(data)


    def make_list_names(self, lists):
        """Turn a list of things into a list of list names"""
        list_names = []
        for l in lists:
            try:
                t = l.list_type
                list_names.append(l.name)
            except AttributeError:
                try: 
                    m = l.model
                    list_names.append(self.create_list(l).name)
                except AttributeError:
                    list_names.append(str(l))

        return list_names

class ListServiceError(IOError):
    """Errors thrown when something goes wrong with list requests"""
    pass
