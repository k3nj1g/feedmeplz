DO $$
DECLARE
    salad_category_id INT;
BEGIN
    SELECT id INTO salad_category_id FROM categories WHERE name = 'Салаты';
    INSERT INTO dishes (name, price, category_id) VALUES
    ('Ветчина с консервированными грибами', 55, salad_category_id),
    ('Ветчина с томатом и сыром', 45, salad_category_id),
    ('Винегрет', 35, salad_category_id),
    ('Витаминный', 35, salad_category_id),
    ('Греческий', 55, salad_category_id),
    ('Зеленый с яйцом', 45, salad_category_id),
    ('Капуста с болгарским перцем и кукурузой', 45, salad_category_id),
    ('Капуста с морской капустой', 55, salad_category_id),
    ('Капуста с овощами', 45, salad_category_id),
    ('Капуста с перцем', 40, salad_category_id),
    ('Капуста с перцем и кукурузой', 45, salad_category_id),
    ('Капуста со свежими овощами', 45, salad_category_id),
    ('Капуста со свежими овощами и оливковым маслом', 45, salad_category_id),
    ('Капуста со стручковой фасолью', 55, salad_category_id),
    ('Консервированные шампиньоны с маринованными огурчиками', 55, salad_category_id),
    ('Копченная грудка с шампиньонами', 55, salad_category_id),
    ('Крабовый с капустой и зернистой горчицей', 50, salad_category_id),
    ('Крабовый с капустой и укропом', 50, salad_category_id),
    ('Крабовый с овощами и сыром', 45, salad_category_id),
    ('Крабовый с пекинкой', 55, salad_category_id),
    ('Крабовый с рисом', 55, salad_category_id),
    ('Крабовый с томатом', 55, salad_category_id),
    ('Крабовый с томатом и чесноком', 50, salad_category_id),
    ('Куриное филе с ананасом и гренками', 55, salad_category_id),
    ('Куриное филе с красной фасолью', 55, salad_category_id),
    ('Куриное филе с томатом и сыром', 55, salad_category_id),
    ('Мимоза', 55, salad_category_id),
    ('Морковь по-корейски', 35, salad_category_id),
    ('Овощной по-грузински', 45, salad_category_id),
    ('Овощной с болгарским перцем и оливковым маслом', 55, salad_category_id),
    ('Овощной с зеленым луком', 45, salad_category_id),
    ('Овощной с зеленым луком и маслом', 45, salad_category_id),
    ('Овощной с зеленым луком и сметаной', 45, salad_category_id),
    ('Овощной с куриным филе', 55, salad_category_id),
    ('Овощной с маслинами', 55, salad_category_id),
    ('Овощной с маслом', 45, salad_category_id),
    ('Овощной с оливками', 55, salad_category_id),
    ('Овощной с оливковым маслом', 55, salad_category_id),
    ('Овощной с пекинкой', 45, salad_category_id),
    ('Овощной с перцем', 55, salad_category_id),
    ('Овощной с перцем и маслом', 50, salad_category_id),
    ('Овощной с редисом', 55, salad_category_id),
    ('Овощной с яйцом', 45, salad_category_id),
    ('Овощной с яйцом и сметаной', 45, salad_category_id),
    ('Овощной со сметаной', 50, salad_category_id),
    ('Оливье', 55, salad_category_id),
    ('Осенний', 45, salad_category_id),
    ('Охотничий', 55, salad_category_id),
    ('Пекинка с ананасом и куриным филе', 55, salad_category_id),
    ('Пекинка с овощами и куриным филе', 55, salad_category_id),
    ('Пекинка с овощами и стручковой фасолью', 60, salad_category_id),
    ('Пекинка с огурцом', 45, salad_category_id),
    ('Пекинка с огурцом и укропом', 45, salad_category_id),
    ('Пекинка с перцем и кукурузой', 55, salad_category_id),
    ('Пекинка с редисом и огурцом', 45, salad_category_id),
    ('Печень куриная с соленым огурцом и горошком', 55, salad_category_id),
    ('Печень куриная с соленым огурцом и овощами', 55, salad_category_id),
    ('Печень куриная с солеными огурцами', 55, salad_category_id),
    ('Печень с гренками и фасолью', 60, salad_category_id),
    ('Русь', 55, salad_category_id),
    ('С ананасом и куриным филе', 55, salad_category_id),
    ('С брокколи и куриным филе', 50, salad_category_id),
    ('С ветчиной и маринованными грибами', 55, salad_category_id),
    ('С ветчиной и пекинкой', 50, salad_category_id),
    ('С ветчиной, томатом и сыром', 45, salad_category_id),
    ('С капустой, свежими овощами и оливковым маслом', 45, salad_category_id),
    ('С консервированным шампиньонами и копченой колбасой', 50, salad_category_id),
    ('С консервированным шампиньонами и маринованными огурцами', 55, salad_category_id),
    ('С копченной грудкой и белой фасолью', 55, salad_category_id),
    ('С копченной грудкой и жаренными шампиньонами', 55, salad_category_id),
    ('С копченной грудкой, орехами и черносливом', 55, salad_category_id),
    ('С копченной колбасой и маринованными шампиньонами', 50, salad_category_id),
    ('С копченной колбасой и свежими овощами', 50, salad_category_id),
    ('С копченой колбасой и шампиньонами', 55, salad_category_id),
    ('С красной фасолью и куриным филе', 55, salad_category_id),
    ('С куриной грудкой и белой фасолью', 55, salad_category_id),
    ('С куриной печенью и маринованными огурцами', 50, salad_category_id),
    ('С куриной печенью и солеными огурцами', 55, salad_category_id),
    ('С куриной печенью, гренками', 55, salad_category_id),
    ('С куриной печенью, гренками и фасолью', 55, salad_category_id),
    ('С куриный филе, ананасом и гренками', 55, salad_category_id),
    ('С куриным филе и томатами', 55, salad_category_id),
    ('С куриным филе пекинкой и ананасом', 50, salad_category_id),
    ('С куриным филе, томатом и сыром', 55, salad_category_id),
    ('С томатом яйцом и сметаной', 40, salad_category_id),
    ('Свекла с орехами', 40, salad_category_id),
    ('Свекла с сыром', 35, salad_category_id),
    ('Свекла с черносливом и курииным филе и орехами', 55, salad_category_id),
    ('Свекла с чесноком', 35, salad_category_id),
    ('Сельдь под шубой', 55, salad_category_id),
    ('Со свежими овощами, редисом и оливковым маслом', 50, salad_category_id),
    ('Тайсон', 65, salad_category_id),
    ('Цезарь с курицей', 60, salad_category_id),
    ('Яйцо c зеленым горошком и майонезом', 45, salad_category_id),
    ('Яйцо под майонезом', 45, salad_category_id),
    ('Яйцо с зеленым горошком', 45, salad_category_id);
END $$;
