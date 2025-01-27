DO $$
DECLARE
    soup_category_id INT;
BEGIN
    SELECT id INTO soup_category_id FROM categories WHERE name = 'Супы';
    INSERT INTO dishes (name, price, category_id) VALUES
    ('Борщ', 40, soup_category_id),
    ('Куриный с рисом', 40, soup_category_id),
    ('Куриный с яйцом', 40, soup_category_id),
    ('Лагман', 50, soup_category_id),
    ('Солянка', 50, soup_category_id),
    ('Суп гороховый', 40, soup_category_id),
    ('Суп грибной', 55, soup_category_id),
    ('Суп куриный с рисом', 40, soup_category_id),
    ('Суп с клецками', 38, soup_category_id),
    ('Суп с лапшой', 38, soup_category_id),
    ('Суп со звездочками', 38, soup_category_id),
    ('Томатный с рисом и свининой', 45, soup_category_id),
    ('Харчо по-грузински', 50, soup_category_id),
    ('Харчо по-домашнему', 55, soup_category_id),
    ('Шулюм по-домашнему', 50, soup_category_id),
    ('Шурпе куриный', 50, soup_category_id),
    ('Щи', 38, soup_category_id);
END $$;
