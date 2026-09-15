# Problem Statement

## Problem Statement

Digital content platforms (e-book libraries, online course providers)
need a way to control what content a user can access based on what they
pay for, while tracking usage so both the user and the platform
understand consumption patterns. Many simple coursework "library
management" systems only model borrowing a physical book with no
concept of tiered access or usage limits. This project addresses that
gap by modeling a **subscription-gated digital content platform**: a
single catalog of mixed media (books and courses) where access to any
given item depends on the user's current subscription tier and their
remaining usage quota for the month.

## Scope of the Project

This project is a **command-line Java application**, run and evaluated
entirely from a terminal, with no GUI or external database dependency.
It covers:

- User registration and authentication (with hashed passwords).
- Three subscription tiers (Free, Basic, Premium), each with a distinct
  monthly access quota and a distinct set of unlockable content.
- A content catalog of two content types — books and courses — modeled
  polymorphically so new content types could be added later with
  minimal changes.
- Enforcement of tier eligibility and quota limits at the point of
  content access, with clear, distinct error conditions for each
  failure mode (expired subscription, insufficient tier, quota
  exhausted).
- Persistent storage of users, catalog content, and access history
  across application runs, using JSON files (no database server
  required).
- Administrator-only functions for catalog management and usage
  reporting/analytics.

Out of scope: payment processing, real content delivery (streaming
video / rendering e-book text), a web or mobile front end, and
multi-user concurrent access (the application is single-session,
single-process).

## Target Users

- **Subscribers** — individuals who register an account, choose a
  subscription tier, and browse/access books and courses within the
  limits of that tier.
- **Platform Administrator** — a single operator role (bootstrapped
  automatically on first run) responsible for maintaining the content
  catalog and reviewing usage analytics across all users.

## High-Level Features

1. **Registration & Login** — email/password accounts with input
   validation and duplicate-email prevention.
2. **Subscription Management** — choose a tier at registration, and
   later upgrade, downgrade, or renew; each tier change resets the
   monthly usage counter and billing cycle.
3. **Content Catalog** — browse all content, search by title or
   category, and (as admin) add or remove books/courses.
4. **Tier-Gated Access** — attempting to access content checks, in
   order: subscription expiry, minimum tier requirement, and remaining
   monthly quota — each with a distinct, user-facing error message.
5. **Usage Tracking & Reporting** — every successful access is logged
   with a timestamp; administrators can view the most accessed content,
   subscriber distribution across tiers, the most active users, and
   access counts broken down by content category.
