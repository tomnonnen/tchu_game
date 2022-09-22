package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Pattern;

import static ch.epfl.tchu.net.Serdes.stringSerde;

/**
 * interface to be implemented in class used to serialize or deserialize object propose a basic serialization for SingleObject/List/SortedBag and methode which build a serde including a serialization function and a deserialization function
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public interface Serde<T> {


    /**
     * @param objetToSerialize : the object to serialize
     * @return the serialization of the object objetToSerialize of type T
     */
    String serialize(T objetToSerialize);

    /**
     * @param str : the string to deserialize
     * @return the object that are coded by the string str
     */
    T deserialize(String str);


    /**
     * build a serde including a serialization function and a deserialization function
     * @param serializeFunction a serialization function
     * @param deserializeFunction a deserialization function
     * @return the corresponding serde
     */
    static <T> Serde<T> of(Function<T,String> serializeFunction,Function<String,T> deserializeFunction){
        return (new Serde<>() {

            /**
             * @param objetToSerialize : the object to serialize
             * @return the application of the function to serialize the objetToSerialize of type T
             */
            @Override
            public String serialize(T objetToSerialize) {
                return serializeFunction.apply(objetToSerialize);
            }

            /**
             * @param str : the string to deserialize
             * @return the application of the function to deserialize the string str
             */
            @Override
            public T deserialize(String str) {
                return deserializeFunction.apply(str);
            }
        });
    }

    /**
     * @param listOfEnumValue the list of all values of an enumerated value set
     * @return the corresponding serde
     */
    static <T> Serde<T> oneOf(List<T> listOfEnumValue){
        return (new Serde<>() {

            /**
             * @param objetToSerialize : the object to serialize
             * @return the index of the objetToSerialize in the list of the enum value
             */
            @Override
            public String serialize(T objetToSerialize) {
                if(objetToSerialize==null) return stringSerde.serialize("");
                return String.valueOf(listOfEnumValue.indexOf(objetToSerialize));
            }

            /**
             * @param str : the string to deserialize
             * @return the object value at the index int(str)
             */
            @Override
            public T deserialize(String str) {
                if(str.equals("")) return null;
                return listOfEnumValue.get(Integer.parseInt(str));
            }
        });
    }


    /**
     * @param serdeToUse serde used to serialize/deserialize each elem of the list
     * @param delimiter the character used to separate the different elements of the List
     * @return a Serd capable of (de)serializing lists of values
     */
    static <T> Serde<List<T>> listOf(Serde<T> serdeToUse,String delimiter) {
        return (new Serde<>() {

            /**
             * @param listToSerialize : the list to serialize
             * @return the serialization of the list with the delimiter and the serde to Use to serialize each element
             */
            @Override
            public String serialize(List<T> listToSerialize) {
                StringJoiner stringJoiner = new StringJoiner(delimiter);
                for (T elemToSerialize : listToSerialize) {
                    stringJoiner.add(serdeToUse.serialize(elemToSerialize));
                }
                return stringJoiner.toString();
            }

            /**
             * @param str : the string to deserialize
             * @return the deserialization of the list with the delimiter and the serde to use to deserialize each element
             */
            @Override
            public List<T> deserialize(String str) {
                List<T> deserializedObject = new ArrayList<>();
                String[] serializedObjectArray = str.split(Pattern.quote(delimiter),-1); //we use "-1", because if the limit is negative then the pattern will be applied as many times as possible and the array can have any length

                for (String serializedObject : serializedObjectArray) {
                    if(!serializedObject.equals("")){
                        deserializedObject.add(serdeToUse.deserialize(serializedObject));
                    }
                }
                return deserializedObject;
            }
        });
    }

    /**
     * @param serdeToUse serde used to serialize/deserialize each elem of the list
     * @param delimiter the character used to separate the different elements of the List
     * @return a Serd capable of (de)serializing SortedBag of values
     */
    static <T extends Comparable<T>> Serde<SortedBag<T>> bagOf(Serde<T> serdeToUse, String delimiter) {
        return (new Serde<>() {

            /**
             * @param sortedBagToSerialize : the sorted bag to serialize
             * @return the serialization of the Sortedbag with the delimiter and the serde to Use to serialize each element
             */
            @Override
            public String serialize(SortedBag<T> sortedBagToSerialize) {
                StringJoiner stringJoiner = new StringJoiner(delimiter);
                for (T listToSerializeElem :sortedBagToSerialize) {
                    stringJoiner.add(serdeToUse.serialize(listToSerializeElem));
                }
                return stringJoiner.toString();
            }

            /**
             * @param str : the string to deserialize
             * @return the deserialization of the Sortedbag with the delimiter and the serde to use to deserialize each element
             */
            @Override
            public SortedBag<T> deserialize(String str) {
                SortedBag.Builder<T> deserializedObject = new SortedBag.Builder<>();
                String[] serializedObjectArray = str.split(Pattern.quote(delimiter),-1); //we use "-1", because if the limit is negative then the pattern will be applied as many times as possible and the array can have any length
                for (String serializedObject : serializedObjectArray) {
                    if(!serializedObject.equals("")){
                        deserializedObject.add(serdeToUse.deserialize(serializedObject));
                    }
                }
                return deserializedObject.build();
            }
        });
    }
}
