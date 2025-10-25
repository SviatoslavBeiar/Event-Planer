import { Route, Routes } from 'react-router-dom';
import './App.css';
import { AuthProvider } from './context/AuthContext';
import Register from './pages/Register';
import Login from './pages/Login'
import Home from './pages/Home';
import Profile from './pages/Profile';
import PostDetails from "./components/PostDetails";
import MyTickets from "./pages/MyTickets";

function App() {
  return (
    <>
      <AuthProvider>
        <Routes>
          <Route path='/' element={<Register />} />
          <Route path='/login' element={<Login />} />
          <Route path='/home' element={<Home />} />
          <Route path='/profile/:userId' element={<Profile />} />
          <Route path="/post/:postId" element={<PostDetails />} />
          <Route path="/tickets" element={<MyTickets />} />
        </Routes>
      </AuthProvider>

    </>
  );
}

export default App;
