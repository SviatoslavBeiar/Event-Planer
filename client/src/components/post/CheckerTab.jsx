
import { Button } from '@chakra-ui/react';
import { Link } from 'react-router-dom';

export default function CheckerTab({ postId }) {
    return (
        <Button as={Link} to={`/verify/${postId}`} colorScheme="blue">
            Open Ticket Verifier
        </Button>
    );
}
