import re
from dataclasses import dataclass

import pymupdf


class PdfExtractionError(Exception):
    """Base error for expected PDF extraction failures."""


class InvalidPdfError(PdfExtractionError):
    """Raised when the input is empty or is not a valid PDF."""


class PdfTooLargeError(PdfExtractionError):
    """Raised when the input exceeds the configured limit."""


class UnreadablePdfError(PdfExtractionError):
    """Raised when PyMuPDF cannot read the document."""


class EncryptedPdfError(PdfExtractionError):
    """Raised when the document requires a password."""


class EmptyPdfTextError(PdfExtractionError):
    """Raised when no readable text can be extracted."""


@dataclass(frozen=True)
class ExtractedPdf:
    text: str
    page_count: int


def extract_pdf_text(pdf_bytes: bytes, max_size: int) -> ExtractedPdf:
    if not pdf_bytes:
        raise InvalidPdfError("PDF file must not be empty.")
    if len(pdf_bytes) > max_size:
        raise PdfTooLargeError(f"PDF file exceeds the maximum allowed size of {max_size} bytes.")
    if not pdf_bytes.startswith(b"%PDF-"):
        raise InvalidPdfError("File content is not a valid PDF.")

    try:
        document = pymupdf.open(stream=pdf_bytes, filetype="pdf")
    except (pymupdf.FileDataError, RuntimeError, ValueError) as error:
        raise UnreadablePdfError("PDF file could not be read.") from error

    try:
        if document.needs_pass:
            raise EncryptedPdfError("Encrypted PDF files are not supported.")
        if document.page_count < 1:
            raise EmptyPdfTextError("PDF contains no readable text.")

        pages = [_clean_page_text(page.get_text("text")) for page in document]
        extracted_text = "\n\n".join(page for page in pages if page).strip()
        if not extracted_text:
            raise EmptyPdfTextError("PDF contains no readable text.")
        return ExtractedPdf(text=extracted_text, page_count=document.page_count)
    except PdfExtractionError:
        raise
    except (pymupdf.FileDataError, RuntimeError, ValueError) as error:
        raise UnreadablePdfError("PDF text could not be extracted.") from error
    finally:
        document.close()


def _clean_page_text(text: str) -> str:
    normalized = text.replace("\r\n", "\n").replace("\r", "\n")
    paragraphs = re.split(r"\n\s*\n", normalized)
    cleaned_paragraphs: list[str] = []
    for paragraph in paragraphs:
        lines = [re.sub(r"[ \t]+", " ", line).strip() for line in paragraph.splitlines()]
        lines = [line for line in lines if line]
        if lines:
            cleaned_paragraphs.append("\n".join(lines))
    return "\n\n".join(cleaned_paragraphs)
