
import { Card, CardHeader, CardBody, Heading, Text, HStack, Badge } from '@chakra-ui/react';
import { QRCodeSVG } from 'qrcode.react';

export default function QrTicketCard({ postId, ticketCode, status }) {
    if (!ticketCode) return null;
    const payload = `TICKET:${postId}:${ticketCode}`;

    const badge = (
        status === 'ACTIVE'    ? <Badge colorScheme="green">ACTIVE</Badge> :
            status === 'USED'      ? <Badge colorScheme="blue">USED</Badge> :
                status === 'CANCELLED' ? <Badge colorScheme="red">CANCELLED</Badge> :
                    <Badge>UNKNOWN</Badge>
    );

    return (
        <Card variant="outline">
            <CardHeader pb={2}>
                <HStack justify="space-between">
                    <Heading size="sm">Your ticket QR</Heading>
                    {badge}
                </HStack>
            </CardHeader>
            <CardBody display="flex" flexDir="column" alignItems="center" gap={2}>
                <QRCodeSVG value={payload} size={200} includeMargin />
                <Text fontSize="xs" color="gray.500" textAlign="center">
                    Show this QR at the entrance. Payload: {payload}
                </Text>
                <Text fontSize="xs" color="gray.500" textAlign="center">
                    This preview does not validate server-side; gate staff will scan your QR.
                </Text>
            </CardBody>
        </Card>
    );
}
