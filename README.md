corm - Pronounced co-RM
========================

CORM is a Codename One sqllite abstraction. 

If you use sqllite often, you will notice typing sql query eveytime is a pain, simply because they can not be validated at compile time.

This is where corm comes in, it abstracts the most general/popular sqllite operations viz

1.	Create table.
2.	Insert into table.
3.	Select from table(s).

-- More to come in future.

corm provides methods with appropriate variables to perform these operations and most of the operation can be checked and validated at compile time.

For easy debugging, generated queries are printed to the standard error.


Usage
-----

```java
DBConnector dbc = new DBConnector();
try {
	dbc.connect("MobInv.sqlite", silent);
} catch (IOException ex) {
	//do something with ex
}

DBQuery dbq = new DBQuery(dbc);

String tableName = "mi_user";
String columnNames[] = {
	"id", "manager", "username", "password", "phone_number", "email_address"
};

String columnTypes[] = {
	"INTEGER", "TEXT", "TEXT", "TEXT", "TEXT", "TEXT"
};

String columnConstraints[] = {
	"CONSTRAINT pk_user_id PRIMARY KEY AUTOINCREMENT",
	"", "", "", "", ""
};

String tableConstraints = "";
try {
	dbq.createTable(tableName, columnNames, columnTypes, columnConstraints, tableConstraints, silent);
} catch (RuntimeException ex) {
	//do something with ex
} catch (IOException ex) {
	//do something with ex
}

See the javadocs for more methods and parameters of DBQuery
```

Installation
-------------
1.	Copy CORM.cn1lib from the dist directory to your projects lib folder
2.	Open your project in the editor of your choice
3.	Right click project and select Refresh Libs.
4.	You can now call corm classes and methods in your project.
