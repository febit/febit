/*
 * Copyright 2017 febit.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.arango;

import com.arangodb.velocypack.VPack;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author zqq90
 */
public class VPackGenericTypesTest {

    public static class Limb {
    }

    public static class Leg extends Limb {

        public String name;

        public Leg() {
        }

        public Leg(String name) {
            this.name = name;
        }
    }

    public static abstract class Animal<L extends Limb> {

        public List<L> arms;
        public L[] legs;
        public L rightLeg;
    }

    public static class Husky extends Animal<Leg> {

        // uncomment and let serialize pass
//        public Leg rightLeg;
//        public Leg[] legs;
        // uncomment and let deserialize pass
//        public List<Leg> arms;
    }

    private static void println(String text) {
        System.out.println(text);
    }

    private static void println() {
        System.out.println();
    }

    public static void main(String[] args) throws Exception {

        printFieldInfo();

        VPack vpack = new VPack.Builder().build();
        Husky tom = new Husky();
        tom.rightLeg = new Leg("right leg");
        tom.arms = new ArrayList<>(Arrays.asList(
                new Leg("right arm"),
                new Leg("left arm")
        ));
        tom.legs = new Leg[]{
            new Leg("right leg"),
            new Leg("left leg")
        };

        Husky tomInMirror = vpack.deserialize(vpack.serialize(tom), Husky.class);
    }

    private static void printFieldInfo() throws NoSuchFieldException {

        Field legsField = Husky.class.getField("legs");
        Field armsField = Husky.class.getField("arms");
        Field rightLegField = Husky.class.getField("rightLeg");

        println("Animal");
        println("Class: " + Animal.class);
        println("TypeParameters[0]: " + Animal.class.getTypeParameters()[0]);

        println();
        println("Husky");
        println("GenericSuperclass: " + Husky.class.getGenericSuperclass());
        println("ActualTypeArguments[0]: " + ((ParameterizedType) Husky.class.getGenericSuperclass()).getActualTypeArguments()[0]);

        println();
        println("arms:");
        println("DeclaringClass: " + armsField.getDeclaringClass());
        println("Type: " + armsField.getType());
        println("GenericType: " + armsField.getGenericType());

        println();
        println("rightLeg:");
        println("DeclaringClass: " + rightLegField.getDeclaringClass());
        println("Type: " + rightLegField.getType());
        println("GenericType: " + rightLegField.getGenericType());
        println("GenericType == Animal.class.getTypeParameters()[0] ?: " + (rightLegField.getGenericType() == Animal.class.getTypeParameters()[0]));

        println();
        println("legs:");
        println("DeclaringClass: " + legsField.getDeclaringClass());
        println("Type: " + legsField.getType());
        println("GenericType: " + legsField.getGenericType());
        if (legsField.getGenericType() instanceof GenericArrayType) {
            println("GenericComponentType legs: " + ((GenericArrayType) legsField.getGenericType()).getGenericComponentType());
        }

    }

}
