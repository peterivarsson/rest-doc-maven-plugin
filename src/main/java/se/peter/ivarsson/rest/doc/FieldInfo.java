/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.peter.ivarsson.rest.doc;

/**
 *
 * @author Peter Ivarsson Peter.Ivarsson@cybercom.com
 */
public class FieldInfo {
   
   String fieldName;
   String fieldType;
   String listOfType = "";   // List of type

   public String getFieldName() {
      return fieldName;
   }

   public void setFieldName( String fieldName ) {
      this.fieldName = fieldName;
   }

   public String getFieldType() {
      return fieldType;
   }

   public void setFieldType( String fieldType ) {
      this.fieldType = fieldType;
   }

   public String getListOfType() {
      return listOfType;
   }

   public void setListOfType( String listOfType ) {
      this.listOfType = listOfType;
   }
   
}
