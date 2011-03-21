package org.intermine.api.util;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */
import java.util.Collection;
import java.util.Set;

import org.intermine.metadata.ClassDescriptor;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.pathquery.Path;
import org.intermine.pathquery.PathException;
import org.intermine.util.DynamicUtil;
import org.intermine.util.TypeUtil;

/**
 * Utility methods for Paths
 * @author Richard Smith
 *
 */
public final class PathUtil
{
    private PathUtil() {
        // don't instantitate
    }

    /**
     * Return the object at the end of a given path, starting from the given object.
     *
     * @param path the path to resolve
     * @param o the start object
     * @return the attribute, object or collection at the end of the path
     * @throws PathException if the path does not match the object type
     */
    public static Object resolvePath(Path path, Object o) throws PathException {
        Model model = path.getModel();
        if (path.getStartClassDescriptor() != null) {
            Set<ClassDescriptor> clds = model.getClassDescriptorsForClass(o.getClass());
            if (!clds.contains(path.getStartClassDescriptor())) {
                throw new PathException("ClassDescriptor from the start of path: " + path
                        + " is not a superclass of the class: "
                        + DynamicUtil.getFriendlyName(o.getClass()) + " while resolving object: "
                        + o, path.toString());
            }
        }
        Object current = o;
        for (String fieldName : path.getElements()) {
            try {
                if (current == null) {
                    return null;
                }
                current = TypeUtil.getFieldValue(current, fieldName);
                if (current instanceof Collection<?>) {
                    throw new RuntimeException("Attempt to to get value of "
                            + "field \"" + fieldName + "\" for collection: " + o
                            + "It must be simple object. This operation is not allowed for "
                            + "collection.");
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("IllegalAccessException while trying to get value of "
                                           + "field \"" + fieldName + "\" in object: " + o, e);
            }
        }
        return current;
    }

    /**
     * Return true if given type (of a constraint) can be assigned to the InterMineObject - i.e.
     * if the class or any superclass of the InterMineObject are the type.  Type can be a qualified
     * or unqualified class name.
     *
     * @param cls the class in the model that will be assigned to
     * @param obj the InterMineObject to check
     * @return a boolean
     */
    public static boolean canAssignObjectToType(Class<?> cls, InterMineObject obj) {
        for (Class<?> c : DynamicUtil.decomposeClass(obj.getClass())) {
            if (cls.isAssignableFrom(c)) {
                return true;
            }
        }
        return false;
    }
}