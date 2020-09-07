# How to use

This project is hosted on AWS.

There are some example commands written below. 

#### Login
```
curl -H "Content-Type: application/json" \
     -d '{ "username": "admin", "password": "admin"}' \
     -X POST 34.207.75.144:8080/auth/login
```
After getting JWT token, use it for other functions.

#### Add Category
##### Add parent category
```
curl -H "Content-Type: application/json" \
     -H  "Authorization: Bearer <<replace with your token>>" \
     -d '{"name": "Kadin", "description" : "Kadin kategorisi", "orderNum" : 1}' \
     -X POST 34.207.75.144:8080/categories
```

##### Add child category
```
curl -H "Content-Type: application/json" \
     -H  "Authorization: Bearer <<replace with your token>>" \
     -d '{"name": "Giyim", "description" : "Kadin giyim kategorisi", "orderNum" : 1, "parentCategory":{"id":1}}' \
     -X POST 34.207.75.144:8080/categories
```

#### Update Category
##### Update child category, "parentCategory" required
```
curl -H "Content-Type: application/json" \
     -H  "Authorization: Bearer <<replace with your token>>" \
     -d '{"name": "Elbise", "description" : "Kadin elbise kategorisi", "orderNum" : 1, "parentCategory":{"id":1}}' \
     -X PUT 34.207.75.144:8080/categories/2
```

##### Update main category
```
curl -H "Content-Type: application/json" \
     -H  "Authorization: Bearer <<replace with your token>>" \
     -d '{"name": "Kadin", "description" : "Kadin kategorisi", "orderNum" : 2}' \
     -X PUT 34.207.75.144:8080/categories/1
```

** Change category hierarcy by changing "parentCategory" property

** Do not give "parentCategory" property if you want to make it main category

#### Delete Category
```
curl -H "Content-Type: application/json" \
     -H  "Authorization: Bearer <<replace with your token>>" \
     -X DELETE 34.207.75.144:8080/categories/3
```

#### Find Category
```
curl -H "Content-Type: application/json" \
     -X GET 34.207.75.144:8080/categories/1
```

#### List Categories
##### List Categories skipChildren=true
Sub categories will be fetched eagerly and included in the result list under their parents.
So, if you want sub categories (children) included alone in the result list, use skipChildren=true
```
curl -H "Content-Type: application/json" \
     -X GET 34.207.75.144:8080/categories?skipChildren=true
```

##### List Categories skipChildren=false
With skipChildren=false, sub categories will not be included alone in the result list, only under their parents.
```
curl -H "Content-Type: application/json" \
     -X GET 34.207.75.144:8080/categories?skipChildren=false
```


#### Add Product
```
curl -H "Content-Type: application/json" \
     -H  "Authorization: Bearer <<replace with your token>>" \
     -d '{"name": "Çok Renkli Volanlı Etek", "description" : "Çok Renkli Volanlı Etek aciklamasi", "remainingStockCount" : 5, "price" : 111.99, "category": { "id": 2 }}' \
     -X POST 34.207.75.144:8080/products
```

#### Update Product
```
curl -H "Content-Type: application/json" \
     -H  "Authorization: Bearer <<replace with your token>>" \
     -d '{"name": "Çok Renkli Volanlı Etek", "description" : "Çok Renkli Volanlı Etek aciklamasi", "remainingStockCount" : 6, "price" : 111.99, "category": { "id": 2 }}' \
     -X PUT 34.207.75.144:8080/products/1
```
** Change product category by changing "category" property

#### Delete Product
```
curl -H "Content-Type: application/json" \
     -H  "Authorization: Bearer <<replace with your token>>" \
     -X DELETE 34.207.75.144:8080/products/1
```

#### Find Product
```
curl -H "Content-Type: application/json" \
     -X GET 34.207.75.144:8080/products/1
```

#### List Products
This service supports paging also.

##### Request params
categoryId: (required) id of category of product

sortBy: default "id"

sortOrder: default "desc"

page: default 0

limit: default 20

```
curl -H "Content-Type: application/json" \
     -X GET 34.207.75.144:8080/products?categoryId=2
```











