import { useEffect, useMemo, useState } from 'react';
import {
    Card,
    CardHeader,
    CardBody,
    Heading,
    Text,
    HStack,
    Badge,
    Spinner,
} from '@chakra-ui/react';
import { QRCodeSVG } from 'qrcode.react';
import TicketService from '../services/TicketService';

export default function QrTicketCard({ postId, ticketCode }) {
    const [isActive, setIsActive] = useState(null); // null -> ще не знаємо
    const [loading, setLoading] = useState(false);
    const svc = useMemo(() => new TicketService(), []);
    const token =
        typeof window !== 'undefined' ? localStorage.getItem('token') : null;

    if (!ticketCode) return null;

    const payload = `TICKET:${postId}:${ticketCode}`;

    useEffect(() => {
        let cancelled = false;

        const load = async () => {
            setLoading(true);
            try {
                const res = await svc.verifyValidate(Number(postId), ticketCode, token);
                if (!cancelled) {
                    // головна логіка: АКТИВНИЙ = valid === true
                    setIsActive(!!res.data?.valid);
                }
            } catch (e) {
                if (!cancelled) {
                    // якщо не змогли дізнатись — вважаємо не активний
                    setIsActive(false);
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        load();

        return () => {
            cancelled = true;
        };
    }, [postId, ticketCode, svc, token]);

    return (
        <Card variant="outline">
            <CardHeader pb={2}>
                <HStack justify="space-between">
                    <Heading size="sm">Your ticket QR</Heading>
                    {loading || isActive === null ? (
                        <Spinner size="sm" />
                    ) : isActive ? (
                        <Badge colorScheme="green">ACTIVE</Badge>
                    ) : (
                        <Badge colorScheme="red">NOT ACTIVE</Badge>
                    )}
                </HStack>
            </CardHeader>
            <CardBody display="flex" flexDir="column" alignItems="center" gap={2}>
                <QRCodeSVG value={payload} size={200} includeMargin />
                <Text fontSize="xs" color="gray.500" textAlign="center">
                    Show this QR at the entrance. Payload: {payload}
                </Text>
            </CardBody>
        </Card>
    );
}
