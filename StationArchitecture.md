# Station architecture: Template + Variant + Config

This codebase models station UI as **reusable templates** driven by **small declarative config**.

## Concepts

- **Template** (`StationTemplateId`)
  - The reusable station “shape” (layout + interaction model).
  - Examples: pick a letter, pop balloons, find in grid, picture-starts-with, etc.

- **Variant** (`StationVariant`)
  - Mode/policy modifiers that still use the same template.
  - Examples: `ListenFirst`, `Episode4Help`, Chapter 3 special modes.
  - Variants are **metadata** for clarity and low-risk UI branching.

- **Config**
  - Concrete text/panel/visibility/help policies live in `StationUiSpec`.
  - Pedagogical/generator behavior lives in `StationQuizPlan` and `LevelSession`.

## Where things live

- **Template + Variant mapping (canonical)**
  - `StationBehaviorRegistry.getStationUiSpec(chapterId, stationId)` sets:
    - `StationUiSpec.templateId`
    - `StationUiSpec.variants`

- **UI config**
  - `StationUiSpec` holds instruction copy, panel styles, help/hint policies, layout flags, etc.

- **Generator semantics**
  - `StationQuizPlan` + `LevelSession` generate questions and determine the learning flow.
  - Do not encode generator semantics into `StationUiSpec` beyond metadata.

## Current template inventory

Defined in `StationTemplateId`:
- `PickLetter`
- `PopBalloons`
- `FindLetterGrid`
- `PictureStartsWith`
- `ImageMatch`
- `MatchLetterToWord`
- `ImageToWord`
- `FinaleSlot` / `Special` (only when required for legacy non-standard flows)

## Current variant inventory

Defined in `StationVariant`:
- `Standard` (always present)
- `ListenFirst`
- `Episode4Help`
- Chapter 3 specials:
  - `Chapter3HighlightedLetter`
  - `Chapter3AudioLetterRecognition`
  - `Chapter3PopAllLettersInWord`
  - `Chapter3ImageToWord`
- `Finale`

## How to add a future Chapter 6

1. Create `StationQuizPlans.chapter6(stationId)` (generator behavior).
2. Add a `StationBehaviorRegistry` mapping for chapter 6 that returns a `StationUiSpec` with:
   - `templateId` set to one of the existing templates
   - `variants` set (at least `Standard`, plus any existing variants needed)
   - UI config fields set explicitly (instruction text, panels, hint/replay policy, etc.)
3. Prefer `templateId` / `variants` checks in UI when a branch is purely presentational.
4. Keep audio timing / sequencing and intro prompt ordering in `GameScreen` unchanged unless intentionally reworked.

