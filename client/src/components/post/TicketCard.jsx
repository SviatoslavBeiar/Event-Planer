
import { Box } from '@chakra-ui/react';
import QrTicketCard from '../QrTicketCard';

export default function TicketCard({ ticket, postId }) {
    return (
        <Box borderWidth="1px" p={4} borderRadius="md">
            <QrTicketCard
                postId={postId}
                ticketCode={ticket.code}
                status={ticket.status}
            />
        </Box>
    );
}
