# Firestore seed: reading lessons

This folder contains a small seeding tool to upload curated `reading_lessons` documents to your Firebase Firestore.

## Prerequisites

- Python 3.9+
- A Firebase service account JSON key with Firestore access

## Install

From repo root:

```bash
py -m venv .venv
.\.venv\Scripts\activate
pip install -r tools/firestore_seed/requirements.txt
```

## Dry run

```bash
py tools/firestore_seed/seed_reading_lessons.py --service-account path\to\serviceAccount.json --dry-run
```

## Seed Firestore

```bash
py tools/firestore_seed/seed_reading_lessons.py --service-account path\to\serviceAccount.json
```

## Notes

- Documents are written to the `reading_lessons` collection.
- Document ID is the lesson `id` (and we also store the same `id` field inside the doc).
- Default behavior is upsert/merge. Use `--force` to overwrite existing docs.
