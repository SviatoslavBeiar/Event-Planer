import { Route, Routes } from 'react-router-dom';
import './App.css';
import { AuthProvider } from './context/AuthContext';

import Register from './pages/Register';
import Login from './pages/Login';
import Home from './pages/Home';
import Profile from './pages/Profile';
import PostDetailsPage from "./pages/PostDetailsPage";
import MyTickets from "./pages/MyTickets";
import VerifierApp from "./pages/VerifierApp";
import VerifierHome from "./pages/VerifierHome";
import PostAnalyticsPage from "./pages/PostAnalyticsPage";
import PaymentSuccessPage from "./pages/PaymentSuccessPage";
import OrganizerRequestsAdmin from "./pages/admin/OrganizerRequestsAdmin";
import RequireAdmin from "./pages/admin/RequireAdmin";
import VerifyEmail from "./pages/VerifyEmail";

function App() {
  return (
      <AuthProvider>
        <Routes>
          <Route path='/' element={<Register />} />
          <Route path='/login' element={<Login />} />
          <Route path='/home' element={<Home />} />
          <Route path='/profile/:userId' element={<Profile />} />


          <Route path="/post/:postId" element={<PostDetailsPage />} />


          <Route path="/posts/:postId" element={<PostDetailsPage />} />

          <Route path="/tickets" element={<MyTickets />} />
          <Route path="/verify" element={<VerifierHome />} />
          <Route path="/verify/:postId" element={<VerifierApp />} />
          <Route path="/post/:postId/analytics" element={<PostAnalyticsPage />} />

          <Route
              path="/admin/organizer-requests"
              element={
                <RequireAdmin>
                  <OrganizerRequestsAdmin />
                </RequireAdmin>
              }
          />

            <Route path="/verify-email" element={<VerifyEmail />} />

          <Route path="/payment/success" element={<PaymentSuccessPage />} />
        </Routes>
      </AuthProvider>
  );
}

export default App;
