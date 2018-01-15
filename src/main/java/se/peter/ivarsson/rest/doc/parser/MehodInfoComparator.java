/*
 * Rest Documentation maven plugin.
 *
 * Copyright (C) 2017 Peter Ivarsson
 */
package se.peter.ivarsson.rest.doc.parser;

import java.util.Comparator;

/**
 * Sort on HttpRequestType and JAX-RS Path
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class MehodInfoComparator implements Comparator<MethodInfo> {

    @Override
    public int compare(MethodInfo o1, MethodInfo o2) {

        if (o1.getMethodPath().equals(o2.getMethodPath())) {

            return o1.getHttpRequestType().compareTo(o2.getHttpRequestType());

        } else {
            
            return o1.getMethodPath().compareTo(o2.getMethodPath());
        }
    }
}

