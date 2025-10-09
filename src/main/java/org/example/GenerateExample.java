package org.example;

import org.example.classes.BinaryTreeNode;
import org.example.classes.Cart;
import org.example.classes.Example;
import org.example.classes.Product;
import org.example.classes.Rectangle;
import org.example.classes.Shape;
import org.example.classes.Triangle;
import org.example.generator.InstanceGenerator;

public class GenerateExample {
    public static void main(String[] args) {
        var instanceGenerator = new InstanceGenerator();
        try {
            var generated1 = instanceGenerator.generateValueOf(Example.class);
            System.out.println(generated1);
            var generated2 = instanceGenerator.generateValueOf(Cart.class);
            System.out.println(generated2);
            var generated3 = instanceGenerator.generateValueOf(Product.class);
            System.out.println(generated3);
            var generated4 = instanceGenerator.generateValueOf(Rectangle.class);
            System.out.println(generated4);
            var generated5 = instanceGenerator.generateValueOf(Triangle.class);
            System.out.println(generated5);
            var generated6 = instanceGenerator.generateValueOf(Shape.class);
            System.out.println(generated6);
            var generated7 = instanceGenerator.generateValueOf(BinaryTreeNode.class);
            System.out.println(generated7);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}