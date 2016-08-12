/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.peter.ivarsson.rest.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class RestInfo {
   
   private List<ClassInfo>                 classInfo     = new ArrayList<>();
   private HashMap<String, DataModelInfo>  domainDataMap = new HashMap<>();

   public List<ClassInfo> getClassInfo() {
      return classInfo;
   }

   public void setClassInfo( List<ClassInfo> classInfo ) {
      this.classInfo = classInfo;
   }

   public HashMap<String, DataModelInfo> getDomainDataMap() {
      return domainDataMap;
   }

   public void setDomainDataMap( HashMap<String, DataModelInfo> domainDataMap ) {
      this.domainDataMap = domainDataMap;
   }

}
