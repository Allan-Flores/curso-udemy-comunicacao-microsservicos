insert into category (id, description) values (1001, 'Comic Books');
insert into category (id, description) values (1002, 'Movies');
insert into category (id, description) values (1003, 'Books');

insert into supplier (id, name) values (1001, 'Panini Comics');
insert into supplier (id, name) values (1002, 'Amazon');


insert into product (id, name, fk_supplier, fk_category, quantity_available, created_at) values (1001, 'Crise nas Infinitas Terras', 1001, 1001, 10, CURRENT_TIMESTAMP);

insert into product (id, name, fk_supplier, fk_category, quantity_available, created_at) values (1002, 'Interestelar', 1002, 1002, 5, CURRENT_TIMESTAMP);

insert into product (id, name, fk_supplier, fk_category, quantity_available, created_at) values (1003, 'Harry Potter E A Pedra Filosofal', 1002, 1003, 3, CURRENT_TIMESTAMP);
