DO $$
DECLARE
    dessert_category_id INT;
BEGIN
    SELECT id INTO dessert_category_id FROM categories WHERE name = 'Десерты';
    INSERT INTO dishes (name, price, category_id) VALUES
    ('Блины 2 шт', 30, dessert_category_id),
    ('Блины 2 шт с джемом', 40, dessert_category_id),
    ('Блины 2 шт со сгущёнкой', 40, dessert_category_id),
    ('Блины 2 шт со сметаной', 40, dessert_category_id),
    ('Джем', 20, dessert_category_id),
    ('Запеканка творожная', 45, dessert_category_id),
    ('Запеканка творожная с джемом', 55, dessert_category_id),
    ('Запеканка творожная со сметаной', 55, dessert_category_id),
    ('Сгущенка', 20, dessert_category_id),
    ('Сметана', 20, dessert_category_id);
END $$;
