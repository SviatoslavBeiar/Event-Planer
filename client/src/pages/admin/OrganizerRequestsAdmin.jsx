import React, { useEffect, useState } from "react";
import {
    Box, Heading, Table, Thead, Tbody, Tr, Th, Td,
    Button, HStack, useToast, Spinner
} from "@chakra-ui/react";
import AuthService from "../../services/AuthService";

export default function OrganizerRequestsAdmin() {
    const toast = useToast();
    const api = new AuthService();
    const [loading, setLoading] = useState(true);
    const [items, setItems] = useState([]);

    const load = async () => {
        setLoading(true);
        try {
            const { data } = await api.adminGetPendingOrganizerRequests();
            setItems(data || []);
        } catch (e) {
            toast({ title: "Load error", description: e?.message || "Failed", status: "error" });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { load(); }, []);

    const approve = async (id) => {
        try {
            await api.adminApproveOrganizerRequest(id);
            toast({ title: "Approved", status: "success" });
            load();
        } catch (e) {
            toast({ title: "Approve error", description: e?.message || "Failed", status: "error" });
        }
    };

    const reject = async (id) => {
        try {
            await api.adminRejectOrganizerRequest(id, { note: "Rejected by admin" });
            toast({ title: "Rejected", status: "success" });
            load();
        } catch (e) {
            toast({ title: "Reject error", description: e?.message || "Failed", status: "error" });
        }
    };

    return (
        <Box p={8}>
            <Heading mb={6}>Organizer requests (PENDING)</Heading>

            {loading ? <Spinner /> : (
                <Table variant="simple">
                    <Thead>
                        <Tr>
                            <Th>ID</Th>
                            <Th>User</Th>
                            <Th>Status</Th>
                            <Th>Created</Th>
                            <Th>Actions</Th>
                        </Tr>
                    </Thead>
                    <Tbody>
                        {items.map((r) => (
                            <Tr key={r.id}>
                                <Td>{r.id}</Td>
                                <Td>{r.userEmail}</Td> {/* або r.userName ... */}
                                <Td>{r.status}</Td>
                                <Td>{r.createdAt}</Td>
                                <Td>
                                    <HStack>
                                        <Button colorScheme="green" size="sm" onClick={() => approve(r.id)}>
                                            Approve
                                        </Button>
                                        <Button colorScheme="red" size="sm" onClick={() => reject(r.id)}>
                                            Reject
                                        </Button>
                                    </HStack>
                                </Td>
                            </Tr>
                        ))}

                    </Tbody>
                </Table>
            )}
        </Box>
    );
}
