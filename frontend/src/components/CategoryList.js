import React from 'react';
import './CategoryList.css';

function CategoryList({ categories }) {
  return (
    <>
      {categories.map((category, index) => (
        <li key={index} className="listItem">
          {category.categoryName} - Budget: ${category.budget}
        </li>
      ))}
    </>
  );
}

export default CategoryList;
