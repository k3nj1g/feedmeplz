import React, { useState } from 'react';
import { 
  Edit, Trash2, Plus, ChevronDown
} from 'lucide-react';
import {
  Card,
  CardContent,
} from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";

const DishCatalog = () => {
  const [openCategory, setOpenCategory] = useState('Салаты');
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingDish, setEditingDish] = useState(null);

  const categories = [
    'Салаты',
    'Супы',
    'Горячие блюда',
    'Гарниры',
    'Десерты'
  ];

  const dishes = {
    'Салаты': [
      { 
        id: 1, 
        name: 'Цезарь с курицей', 
        description: 'Салат романо, куриное филе, гренки, пармезан, соус цезарь', 
        price: 420, 
        weight: 220,
        calories: 350
      },
      { 
        id: 2, 
        name: 'Греческий', 
        description: 'Свежие овощи, маслины, сыр фета, оливковое масло', 
        price: 380, 
        weight: 200,
        calories: 280
      }
    ],
    'Супы': [
      { 
        id: 3, 
        name: 'Борщ', 
        description: 'Традиционный борщ со сметаной', 
        price: 320, 
        weight: 300,
        calories: 240
      },
      { 
        id: 4, 
        name: 'Куриный суп', 
        description: 'Куриный бульон с лапшой и овощами', 
        price: 280, 
        weight: 300,
        calories: 180
      }
    ],
  };

  const handleEditClick = (dish) => {
    setEditingDish({ ...dish });
    setIsEditModalOpen(true);
  };

  const handleSave = () => {
    // Here you would typically update the dish in your database
    console.log('Saving dish:', editingDish);
    setIsEditModalOpen(false);
    setEditingDish(null);
  };

  const renderDish = (dish) => (
    <Card key={dish.id} className="bg-white">
      <CardContent className="p-4">
        <div className="flex justify-between items-start">
          <div className="flex-1">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="font-medium text-lg mb-1">{dish.name}</h3>
                <p className="text-sm text-gray-600">{dish.description}</p>
              </div>
              <div className="flex space-x-2">
                <button 
                  className="p-2 text-gray-500 hover:text-blue-500"
                  onClick={() => handleEditClick(dish)}
                >
                  <Edit className="w-4 h-4" />
                </button>
                <button className="p-2 text-gray-500 hover:text-red-500">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
            <div className="flex space-x-4 mt-2 text-sm text-gray-500">
              <span>{dish.price} ₽</span>
              <span>{dish.weight} г</span>
              <span>{dish.calories} ккал</span>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );

  return (
    <div className="max-w-6xl mx-auto px-4 py-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-semibold">Каталог блюд</h1>
        <button 
          className="flex items-center px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600"
          onClick={() => {
            setEditingDish({ name: '', description: '', price: 0, weight: 0, calories: 0 });
            setIsEditModalOpen(true);
          }}
        >
          <Plus className="w-4 h-4 mr-2" />
          Добавить блюдо
        </button>
      </div>

      <div className="flex">
        <div className="w-64 pr-6">
          <div className="bg-white rounded-lg shadow">
            {categories.map(category => (
              <button
                key={category}
                className={`w-full text-left px-4 py-3 hover:bg-gray-50 ${
                  openCategory === category ? 'bg-blue-50 text-blue-600' : 'text-gray-700'
                }`}
                onClick={() => setOpenCategory(category)}
              >
                {category}
              </button>
            ))}
          </div>
        </div>

        <div className="flex-1 space-y-4">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-medium">{openCategory}</h2>
          </div>
          {dishes[openCategory]?.map(dish => renderDish(dish))}
        </div>
      </div>

      <Dialog open={isEditModalOpen} onOpenChange={setIsEditModalOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{editingDish?.id ? 'Редактировать блюдо' : 'Новое блюдо'}</DialogTitle>
          </DialogHeader>
          
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <label htmlFor="name" className="text-sm font-medium">Название</label>
              <Input
                id="name"
                value={editingDish?.name}
                onChange={(e) => setEditingDish({ ...editingDish, name: e.target.value })}
              />
            </div>
            
            <div className="grid gap-2">
              <label htmlFor="description" className="text-sm font-medium">Описание</label>
              <Textarea
                id="description"
                value={editingDish?.description}
                onChange={(e) => setEditingDish({ ...editingDish, description: e.target.value })}
              />
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div className="grid gap-2">
                <label htmlFor="price" className="text-sm font-medium">Цена (₽)</label>
                <Input
                  id="price"
                  type="number"
                  value={editingDish?.price}
                  onChange={(e) => setEditingDish({ ...editingDish, price: Number(e.target.value) })}
                />
              </div>
              
              <div className="grid gap-2">
                <label htmlFor="weight" className="text-sm font-medium">Вес (г)</label>
                <Input
                  id="weight"
                  type="number"
                  value={editingDish?.weight}
                  onChange={(e) => setEditingDish({ ...editingDish, weight: Number(e.target.value) })}
                />
              </div>

              <div className="grid gap-2">
                <label htmlFor="calories" className="text-sm font-medium">Калории</label>
                <Input
                  id="calories"
                  type="number"
                  value={editingDish?.calories}
                  onChange={(e) => setEditingDish({ ...editingDish, calories: Number(e.target.value) })}
                />
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditModalOpen(false)}>
              Отмена
            </Button>
            <Button onClick={handleSave}>
              Сохранить
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default DishCatalog;
