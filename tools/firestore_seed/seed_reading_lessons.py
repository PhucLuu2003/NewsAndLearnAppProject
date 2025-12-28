import argparse
import json
import os
import sys
from datetime import datetime, timezone


def _require_env(var_name: str) -> str:
    value = os.getenv(var_name)
    if not value:
        raise SystemExit(f"Missing required env var: {var_name}")
    return value


def _utc_now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def _load_seed_json(path: str):
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)

    if not isinstance(data, list):
        raise SystemExit("Seed JSON must be a list of lesson objects")

    for i, lesson in enumerate(data):
        if not isinstance(lesson, dict):
            raise SystemExit(f"Lesson at index {i} must be an object")
        if not lesson.get("id"):
            raise SystemExit(f"Lesson at index {i} is missing required field: id")
        if not lesson.get("title"):
            raise SystemExit(f"Lesson {lesson.get('id')} is missing required field: title")
        if not lesson.get("passage"):
            raise SystemExit(f"Lesson {lesson.get('id')} is missing required field: passage")

    return data


def main():
    parser = argparse.ArgumentParser(
        description="Seed Firestore reading_lessons with curated content."
    )
    parser.add_argument(
        "--service-account",
        required=True,
        help="Path to Firebase service account JSON",
    )
    parser.add_argument(
        "--seed",
        default=os.path.join(os.path.dirname(__file__), "reading_lessons_seed.json"),
        help="Path to seed JSON file",
    )
    parser.add_argument(
        "--collection",
        default="reading_lessons",
        help="Firestore collection name",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Validate and print what would be written without writing",
    )
    parser.add_argument(
        "--force",
        action="store_true",
        help="Overwrite existing docs (default: merge/upsert)",
    )

    args = parser.parse_args()

    lessons = _load_seed_json(args.seed)

    if args.dry_run:
        print(f"Loaded {len(lessons)} lessons from {args.seed}")
        for lesson in lessons:
            print(f"- {lesson['id']}: {lesson.get('title','')} ({lesson.get('level','')}/{lesson.get('category','')})")
        print("Dry run: no writes performed")
        return

    # Import Firebase Admin lazily so dry-run works without deps.
    try:
        import firebase_admin
        from firebase_admin import credentials, firestore
    except Exception as e:
        raise SystemExit(
            "firebase-admin is required. Install with: pip install -r requirements.txt\n"
            f"Import error: {e}"
        )

    if not os.path.isfile(args.service_account):
        raise SystemExit(f"Service account file not found: {args.service_account}")

    # Initialize app
    if not firebase_admin._apps:
        cred = credentials.Certificate(args.service_account)
        firebase_admin.initialize_app(cred)

    db = firestore.client()

    batch = db.batch()
    write_count = 0

    for lesson in lessons:
        doc_id = lesson["id"]
        doc_ref = db.collection(args.collection).document(doc_id)

        payload = dict(lesson)

        # Prefer Firestore-native timestamps for easier querying/sorting.
        payload.setdefault("createdAt", firestore.SERVER_TIMESTAMP)
        payload["updatedAt"] = firestore.SERVER_TIMESTAMP

        # Keep Firestore doc id and data id aligned
        payload["id"] = doc_id

        if args.force:
            batch.set(doc_ref, payload)
        else:
            batch.set(doc_ref, payload, merge=True)

        write_count += 1

        # Firestore batch limit is 500 operations
        if write_count % 450 == 0:
            batch.commit()
            batch = db.batch()

    batch.commit()
    print(f"Seeded/updated {write_count} documents into collection '{args.collection}'.")


if __name__ == "__main__":
    main()
