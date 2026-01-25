import React, { useContext } from "react";
import { Navigate } from "react-router-dom";
import AuthContext from "../../context/AuthContext";

export default function RequireAdmin({ children }) {
    const { user } = useContext(AuthContext);

    if (!user) return <Navigate to="/login" replace />;
    if (user.role !== "ADMIN") return <Navigate to="/" replace />;

    return children;
}
