import CategoryForm from '../components/CategoryForm';
import CategoryList from '../components/CategoryList';

export default function Categories() {
  return (
    <div className="container">
      <h1>Categories</h1>
      <CategoryForm />
      <ul><CategoryList /></ul>
    </div>
  );
}
