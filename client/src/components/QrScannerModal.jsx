// src/components/QrScannerModal.jsx
import { useEffect, useRef, useId } from 'react';
import { Modal, ModalOverlay, ModalContent, ModalHeader, ModalBody, ModalCloseButton, Box } from '@chakra-ui/react';
import { Html5Qrcode } from 'html5-qrcode';

export default function QrScannerModal({ isOpen, onClose, onDetected }) {
    const html5Ref = useRef(null);
    const runningRef = useRef(false);
    const divId = useId(); // унікальний id для контейнера

    const stopSafe = async () => {
        const inst = html5Ref.current;
        if (!inst) return;
        try {
            if (runningRef.current) {
                await inst.stop();
            }
        } catch (e) {
            if (!String(e?.message || e).includes('scanner is not running')) console.warn(e);
        }
        try { await inst.clear(); } catch (_) {}
        html5Ref.current = null;
        runningRef.current = false;
    };

    const start = async () => {
        await stopSafe(); // гарантуємо чистий старт
        const elementId = `qr-reader-${divId}`;
        const inst = new Html5Qrcode(elementId);
        html5Ref.current = inst;
        try {
            await inst.start(
                { facingMode: 'environment' },
                { fps: 10, qrbox: 250 },
                (decodedText) => {
                    runningRef.current = true;
                    onDetected?.(decodedText);
                },
                // decode error callback — не показуємо в UI
                () => {}
            );
            runningRef.current = true;
        } catch (e) {
            runningRef.current = false;
            // eslint-disable-next-line no-console
            console.error('QR start failed', e);
            await stopSafe();
        }
    };

    useEffect(() => {
        if (isOpen) start();
        return () => { stopSafe(); };

    }, [isOpen]);


    return (
        <Modal isOpen={isOpen} onClose={async () => { await stopSafe(); onClose?.(); }}>
            <ModalOverlay />
            <ModalContent>
                <ModalHeader>Scan ticket QR</ModalHeader>
                <ModalCloseButton />
                <ModalBody>
                    <Box id={`qr-reader-${divId}`} w="100%" minH="280px" />
                </ModalBody>
            </ModalContent>
        </Modal>
    );
}
