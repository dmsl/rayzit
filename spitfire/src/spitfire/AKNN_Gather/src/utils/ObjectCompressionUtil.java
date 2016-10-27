/**
  * Spitfire: A distributed main-memory algorithm for AkNN query processing
  *
  * Copyright (c) 2015 Data Management Systems Laboratory, University of Cyprus
  *
  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.
  * If not, see http://www.gnu.org/licenses/.
  *
 */
/**
 * Created by Constantinos Costa.
 * Copyright (c) 2015 DMSL. All rights reserved
 */
package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
 
/**
 * The Class ObjectCompressionUtil.
 * 
 * @param <T> the generic type of the serializable object to be compressed
 */
public class ObjectCompressionUtil<T extends Serializable> {
 
 /**
  * Compress object.
  * 
  * @param objectToCompress the object to compress
  * @param outstream the outstream
  * @return the compressed object
  * @throws IOException Signals that an I/O exception has occurred.
  */
 public T compressObject(final T objectToCompress, final OutputStream outstream) throws IOException {
 
  final GZIPOutputStream gz = new GZIPOutputStream(outstream);
  final ObjectOutputStream oos = new ObjectOutputStream(gz);
   
  try {
  oos.writeObject(objectToCompress);
  oos.flush();
  return objectToCompress;
  }finally {
   oos.close();
   outstream.close();
  }
 
 }
 
 /**
  * Expand object.
  * 
  * @param objectToExpand the object to expand
  * @param instream the instream
  * @return the expanded object
  * @throws IOException Signals that an I/O exception has occurred.
  * @throws ClassNotFoundException the class not found exception
  */
 public T expandObject(final T objectToExpand, final InputStream instream) throws IOException,
   ClassNotFoundException {
  final GZIPInputStream gs = new GZIPInputStream(instream);
  final ObjectInputStream ois = new ObjectInputStream(gs);
 
  try {
   @SuppressWarnings("unchecked")
   T expandedObject = (T) ois.readObject();
   return expandedObject;
  } finally {
   gs.close();
   ois.close();
  }
 }
 
}