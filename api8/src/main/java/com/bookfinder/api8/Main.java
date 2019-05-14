package com.bookfinder.api8;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        HttpClientConfig config = new HttpClientConfig();
        Scanner sc = new Scanner(System.in);

        System.out.println("Search terms: ");
        String searchTerms = sc.nextLine();

        List<Book> books = HttpClientConfig.fetchData(searchTerms);

        int i = 1;

        for (Book book : books) {
            System.out.println(i + ". " + book.getTitle() + " (" + book.getPublishedDate() + ")");

            if (book.getSubtitle() != null) {
                System.out.println(book.getSubtitle());
            }
            System.out.println("Author(s): " + book.getAuthors());
            System.out.println("Price: " + book.getListPrice() + " instead of " + book.getRetailPrice());
            System.out.println(book.getDescription());
            System.out.println();
            i++;
        }

        sc.close();
    }
}
