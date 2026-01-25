import React, { useEffect, useState } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { Box, Heading, Text, Spinner, Button } from "@chakra-ui/react";
import AuthService from "../services/AuthService";

export default function VerifyEmail() {
    const [params] = useSearchParams();
    const token = params.get("token");
    const api = new AuthService();

    const [loading, setLoading] = useState(true);
    const [ok, setOk] = useState(false);
    const [err, setErr] = useState("");

    useEffect(() => {
        (async () => {
            try {
                await api.verifyEmail(token);
                setOk(true);
            } catch (e) {
                setErr(e?.response?.data || e?.message || "Verification failed");
            } finally {
                setLoading(false);
            }
        })();
    }, [token]);

    return (
        <Box p={10}>
            <Heading mb={4}>Email verification</Heading>
            {loading ? <Spinner /> : ok ? (
                <>
                    <Text mb={4}>✅ Account activated. You can log in now.</Text>
                    <Button as={Link} to="/login" colorScheme="blue">Go to Login</Button>
                </>
            ) : (
                <>
                    <Text mb={4}>❌ {err}</Text>
                    <Button as={Link} to="/login">Back to Login</Button>
                </>
            )}
        </Box>
    );
}
