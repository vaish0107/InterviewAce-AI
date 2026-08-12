import re
import unicodedata
from dataclasses import dataclass

from app.core.skills import SKILL_CATALOG


@dataclass(frozen=True)
class ExtractedSkills:
    skills: list[str]
    categories: dict[str, list[str]]


class SkillExtractor:
    """Matches a curated skill catalog without probabilistic or external services."""

    def extract(self, text: str) -> ExtractedSkills:
        normalized_text = self._normalize(text)
        detected_skills: list[str] = []
        detected_categories: dict[str, list[str]] = {}

        for category, catalog_skills in SKILL_CATALOG.items():
            category_matches: list[str] = []
            for canonical_name, aliases in catalog_skills.items():
                if any(self._matches(normalized_text, alias) for alias in aliases):
                    detected_skills.append(canonical_name)
                    category_matches.append(canonical_name)
            if category_matches:
                detected_categories[category] = category_matches

        return ExtractedSkills(skills=detected_skills, categories=detected_categories)

    @staticmethod
    def _normalize(text: str) -> str:
        normalized = unicodedata.normalize("NFKC", text).lower()
        normalized = re.sub(r"[\u2010-\u2015]", "-", normalized)
        return re.sub(r"\s+", " ", normalized).strip()

    @staticmethod
    def _matches(normalized_text: str, alias: str) -> bool:
        normalized_alias = SkillExtractor._normalize(alias)
        # Spaces and hyphens are treated as equivalent separators, while punctuation
        # such as +, #, /, and . remains meaningful for C++, C#, CI/CD, and Node.js.
        parts = re.split(r"[\s-]+", normalized_alias)
        expression = r"[\s-]+".join(re.escape(part) for part in parts)
        pattern = rf"(?<![a-z0-9]){expression}(?![a-z0-9])"
        return re.search(pattern, normalized_text) is not None
