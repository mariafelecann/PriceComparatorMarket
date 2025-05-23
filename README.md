# PriceComparatorMarket

This project is a backend server for a price comparison platform. 
It provides its users with the following features:

1. ### Overview

- `Optimizing Shopping Baskets` 
    - Users can split their shopping baskets into shopping lists. They enter what products they need, as well as their quantity, and the server returns the best discounts for those products, sorted into shopping lists. Each shopping list is associated with a store.
- `Discovering Discounts`
  Users can see the best active discounts and the discounts that have been added recently.
- `Analyzing Price Trends`
  - Users can get historical price data for products, filterable by store, category, or brand
- `Custom Price Alerts`
  - Users can set price thresholds and automatically detect when a product's price drops to their target

2. ### Instructions on how to run this app

- Clone this repo:

```bash
    git clone https://github.com/mariafelecann/PriceComparatorMarket.git
```

- Build the project with maven

  mvn clean install

- Run the application

3. ### Routes

-  **/import/all**

Upon the first run (or if the H2 database is reset), you'll need to import the sample CSV data.
Ensure your CSV data files are located in `src/main/resources/csv/`.
Once the application is running, trigger the import via a POST request:
    ```
    POST http://localhost:8080/api/import/all
    Content-Type: application/json
    ```
  (Body can be empty or `{}`).

- **discounts/best**
  Lists products with the highest current percentage discounts.
- **discounts/new**

Lists discounts that started on a specific date

- **products/price-history**

Provides historical price data for a product or category or brand, filterable by date range and store

- **products/recommendations**
  Recommends alternative products from the same category, ordered by "value per unit"

- **alerts/price**

Creates a new price alert for a specific product at a target price

4. ### Assumptions and Simplifications

- The application uses an H2 in-memory database for development and testing. This means all data is lost when the application restarts. For persistent data, you'd configure a different database
- The `CSVManagerService` assumes specific column orders and delimiters (semicolon `;`) for the CSV files. Any deviation in the CSV structure will require code changes in `CSVManagerService`
- This is only a backend server and can be tested with tools like Postman
- The routes are not secured and do not need any authentication


