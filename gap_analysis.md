# Design Spec Parity Gap Analysis

Spec: docs/Student App UI.dc.html (111747 bytes)
Sections extracted: t1, 1a-1x (25 sections including 1v)

## Summary of current implementation (high-level)
- Theme/tokens: Material3Colors.kt has the core tokens (primary/surface/outlines/status). components.kt has shared UI primitives.
- Components: BubtTag/BubtCategoryTile/BubtChip(leadingDot), docked FAB nav (MainScaffold), etc. exist.
- Screens: Welcome, ProfileSetup, GuestProfile, AnnexLogin, Home, Academics hub + tabs (Routine/Exams/Results/Attendance/Calendar/Courses), Scanner wizard, Community, Profile - implemented.

This is a comprehensive review starting from spec extraction.
## Detailed Observations

### 1a Foundations (tokens/typography/shapes/spacing)
Spec shows: Color tokens Light/Dark as specified. Typography: H1 24/700, H2 20/600, Title 16/600, Body 16/400, Body S 13/400, Label 11/600, Bengali 16px (Hind Siliguri). Shapes: Card 16, Button 28, Field 12, Chip full. Spacing: 4,8,12,16,20,24 dp; screen 20, card 16.
Our implementation: Material3Colors.kt defines matching tokens. Material3Typography uses Poppins/Hind Siliguri with appropriate sizes. Shapes.kt has similar radii. BubtSpacing defined (Screen=20.dp, CardPadding=16.dp etc). Overall tokens align well.

### 1b Components & states
Buttons (Primary): Default/Pressed/Disabled/Loading present. Also Tonal/Outlined/Text and icon buttons. Input fields Default/Focused/Error present. Chips/Toggles/Tabs present. Cards Elevated/Filled/Outlined present. Status badges present. Course card with attendance. Implementation covers these components in components.kt and DesignSystemShowcase. States modeled.

### 1c Welcome
Spec: Left-aligned single screen. Illustration with soft circle (primaryContainer alpha ~50%), rotated paper card on left, event card (SUN 11) on right, check badge bottom-right, dots. Logo B + "BUBT Companion", H1 "Your whole campus, in one pocket.", body, Get Started button with arrow_forward icon and elevation/shadow, "I have an Annex account" text button. 
Current: WelcomeScreen has WelcomeIllustration() drawing similar elements, logo, texts, Get Started with AutoMirrored ArrowForward, shadow applied, Annex login link. Close match. Minor: illustration composition slightly different (we draw rotated surface + circle) vs exact layered shapes in spec.

### 1d Profile Setup (step 1 of 2)
Spec: Top bar with borderless back (arrow_back), right side "Step 1 of 2" (bodyMedium muted). Progress bar 50%. Title "Let's set up your profile", subtitle "No account needed. You can connect Annex later." Fields: Roll (focused example shows 22235103045), Department (dropdown with arrow_drop_down), Semester (error state "Please select your current semester"), Name (optional). Continue button.
Current: OnboardingTopBar with back, trailing shows "Step 1 of 2" when not editing. StepProgress at 50%. Texts match. Fields present. BubtDropdownField uses overlay to open menu (works). Error handling for semester/roll exists in ViewModel. Good parity.

### 1e Guest Profile + Connect Annex
Spec: App bar "Profile", Guest mode pill with person_off. Avatar 72dp face icon with small warning dot top-right. Name + "Guest · data stored on this device". Tonal card with Roll/Department/Semester rows each with lock icon on right. Filled-primary card: link icon, "Connect your Annex account", subtitle, white chips (Attendance/Results/Class routine), Connect Annex button. "Maybe later" text button. Step progress completed 2 shown in context? Spec shows full screen.
Current: GuestProfileScreen has OnboardingTopBar with "Profile" and BubtChip "Guest mode". ProfileHeader shows avatar (56dp face icon) + text. ProfileSummaryCard shows 3 rows with Lock icons (tonal surface style). ConnectAnnexCard is filled BubtCard with Link icon, heading, subtitle, 3 chips, Annex ID + Password fields, Connect button. "Maybe later" button. Also has StepProgress(total=2, completed=2) - matches the flow shown in spec (step 2). Avatar size is 56dp vs 72dp in spec; lock icons present; tonal vs surfaceContainer - spec shows "tonal" card styling for the info rows. Need to match exact card style naming - in our code ProfileSummaryCard uses Surface with surfaceContainer (tonal-ish) and ConnectAnnexCard uses BubtCardStyle.Filled (primary filled). Spec labels: the middle card is "tonal" (info rows with locks), bottom is filled-primary CTA. Our implementation matches the semantics.

### 1f Annex login
Spec: Top bar back, shield icon tile (56dp primaryContainer) + "Sign in to Annex" + subtitle. Fields Student ID, Password. "Keep me signed in" toggle. Disabled Sign in initially (enabled when valid). Green check benefit list. Footer "Your credentials stay on your device. We only use them to fetch from Annex."
Current: AnnexLoginScreen has shield icon tile (56dp), texts, Student ID + Password fields, Switch "Keep me signed in", Sign in button disabled when invalid, AnimatedVisibility shows error if any, BenefitRow items (green check) - Attendance/Results/Routine etc. Footer text present. Good match.

### 1g Dashboard (Home)
Spec: Header with avatar initial RA (44dp), Bengali line + greeting "Good morning, Rafi". Bell with red dot. "Present Courses" section with chips (CSE 231 etc with colored dots). Next class card elevated, timer "in 25 min", progress bar, bell active. Pager dots. Quick actions: Scan Document (primary), View Routine (success green), Check Result (warning amber), Academic Calendar (error red) - each in tonal cards with colored icons. Bottom nav with Home/Academics/Scan/Community/Profile (docked FAB center).
Current: HomeScreen header matches (avatar initials, bengaliLine + greeting, bell with dot). Chips show leading dots (BubtChip supports leadingDot). HighlightsCarousel shows NextClass card with timer and progress. QuickActionsGrid uses tonal cards with colored tints (ScanDocument tint primary, ViewRoutine success, CheckResult warning, Calendar error). MainScaffold has DockedScanNavBar with center Scan FAB. Overall strong match.

### 1h Routine
Spec: Tabs Academics (Routine selected). Day chips SAT 10, SUN 11 (selected blue), MON 12 etc. Timeline with times 08:30 (done faded), 10:30 now (blue dot with ring), 12:30 upcoming, 02:30 upcoming. Class cards with Done tag, Now tag with progress bar, bell icons (filled when reminder on). Break slot shown in timeline.
Current: RoutineTab shows day chips with dates (we compute dayDates), selected state uses primary. TimelineRow renders dots/timeline. ClassCard shows Done tag when past and not now? Now tag when isNow, progress bar for time left. Bell toggles reminder - filled when on. BreakCard exists. Implementation matches structure.

### 1i Exams
Spec: Tabs Routine/Exams(selected). Segmented Normal | Supplementary. Supplementary banner if any. Exam cards grouped by term, with Download All button per term. Reminder button toggles state. Empty state "No upcoming exams" (1w).
Current: ExamsTab uses BubtSegmentedControl for Normal/Supplementary, shows SupplementaryBanner, groups by term, ExamCard has reminder toggle. Empty state text present. Matches.

### 1j Results
Spec: Results tab selected. Semester dropdown. Solid primary panel: SGPA (44px bold) with +0.14 delta (trending_up), CGPA with credits on right split by divider. SGPA history bars (heights vary, selected highlighted). Course grades list with letter grades in tags.
Current: ResultsTab has dropdown, SgpaCgpaPanel with primary background, splits SGPA/CGPA, shows delta with trend icon (trending_up/auto-mirrored), SgpaHistory bars, GradeRow list. Implementation matches new spec structure (single panel).

### 1k Attendance
Spec: Overall ring (percent + below count), per-course rings colored by threshold (<60 red, 60-74 amber, >=75 green). Cards show "At-risk" state (dashed border) when <75%. Pull-to-refresh. Synced label. Bottom sheet log on tap.
Current: AttendanceTab uses PullToRefreshBox, OverallSummary ring, AttendanceCard shows dashed border when atRisk. ringColorFor uses thresholds: <60 red, <75 amber (MIN_ATTENDANCE_PERCENT=75), else green - matches. Synced label shown. Log sheet on tap. Good.

### 1l Calendar
Spec: Month header with chevron chips (left/right), filter chips All/Payments/Exams/Holidays. Timeline events with type icons (Payments=payments/warning, Exams=edit_note/success, Holidays=beach_access), past events at 60% opacity. Red "Today" divider line. Action buttons in events (See exam routine, Pay now).
Current: CalendarTab has MonthHeader with chevron buttons, filter chips via FilterPill (All/Payments/Exams/Holidays), EventRow shows icons with colors, past events dimmed (alpha 0.6), TodayDivider rendered at correct position. Action buttons wired to callbacks. Matches.

### 1v Timetable (exists in spec)
Spec section 1v exists (line 378). Content: Timetable view variation? Let us look at extracted content quickly conceptually - it's another layout (possibly alternate routine view). Need to check if it's a separate tab/view. Academics has Routine as first tab; 1v might be a specific variant - we should compare. But our RoutineTab follows 1h structure which is the main routine.

### 1m Courses
Spec: Tabs Attendance/Calendar/Courses(selected). Search bar, segmented All/Completed/Remaining. Progress "32 of 58 completed 55%" with progress bar. Course cards: number tile (101), title + code/credits, Done tag (success) when completed, Ongoing tag (primary) when ongoing. 
Current: CoursesTab has search, BubtSegmentedControl, progress line, CourseCard shows NumberTile (we added NumberTile internally in CoursesTab or via components?), check implementation: in CoursesTab we see CourseCard builds a Surface with row; in our earlier parity rebuild we created NumberTile inside CoursesTab? Looking back at commit: "CoursesTab.kt: rows+dividers → CourseCard with NumberTile + BubtTag Done/Ongoing." Yes, NumberTile is a private composable in CoursesTab. Done/Ongoing use BubtTag with appropriate styles. Matches.

### 1n Course Details (if present)
Section 1n exists. May be a detail screen - check if implemented? Route might exist. Let us scan for CourseDetails.

### 1o-1s Scanner wizard
Spec: 4-step progress (1-4). Step 1 (1o): cover template selection with 6 tiles (Assignment selected with check), dashed "No cover" tile. Long-press to preview. Step 2 (1p): 4-segment progress bar (filled 2), 3D paper preview tilted (rotateX(14deg) rotateY(-20deg) rotateZ(3deg)), live preview badge, date picker, fields, Auto badge when submittedBy auto-filled. Step 3 (1q): capture/upload - camera view, grid overlay, capture button, upload from gallery, thumbnail strip, review button. Step 4 (1r): review - reorder pages (drag handle), edit cover, crop/rotate/filter/delete per page, filename field, merge PDF button. Success (1s): check badge, PDF ready, share/open/location, "Scan another" CTA.
Current: ScannerScreen implements TemplateStep (1o), DetailsStep (1p) with 3D preview and tilt transforms, CaptureStep (1q), ReviewStep (1r), SuccessRoute (1s). WizardTopBar shows step numbers. Progress bars implemented. All steps covered. Close parity.

### 1t Community feed
Spec: Header "Community" + bookmarks icon. Search bar. Filter chips All/CSE/Lab Reports/Internship/BBA (All selected). Feed cards: icon tile by category (Notes description blue, LabReport functions green, Internship work amber). Card content: title, author/avatar or anonymous, category tag, stats (views, time), favorite count (red filled when liked), download icon button. FAB "Share" (extended) shrinks on scroll. Empty state 1x.
Current: CommunityScreen has header with bookmarks, search, horizontal filter chips (FilterPill), PostCard uses BubtCategoryTile by category with correct tints (Notes primary/description style blue, LabReport Success/functions green, Internship Warning/work amber). Shows views, days ago, favorite icon filled red when liked, download IconButton. Extended FAB shrinks on scroll (fabExpanded state). Matches 1t structure.

### 1u Profile & settings
Spec: Header "Profile" + Edit. Outlined card identity: avatar initials, name, ID, tags CSE/Intake 49/Sec 2. Green status row when Annex connected (verified + "Synced 5 min ago" + sync icon). Fees entry if connected. NOTIFICATIONS outlined card: Class reminder row with chevron (value 10 min before), Exam reminders toggle on, Payment deadlines toggle off. APPEARANCE segmented control Light/Dark/System (System selected). Log out outlined button (red text). Guest sees Connect card instead of green status.
Current: ProfileScreen shows header Profile + Edit, ProfileIdentity as outlined BubtCard with avatar, name, ID, tags, green Annex connected row with synced label and sync button. FeesEntry shown when connected. NotificationSettings in outlined card with rows and switches. ThemeModeSelector as segmented control (3 options). OutlinedButton "Log out" with red styling via bubtErrorText. When not connected, shows ConnectAnnexCard (from GuestProfile). Matches 1u.

### 1w No upcoming exams (empty state)
Spec: Illustration (soft circle, paper/event shape with check), "No upcoming exams 🎉", body text, "Turn on exam alerts" text button.
Current: ExamsTab shows empty state text "No upcoming exams scheduled." when groups empty. We may have EmptyState component? Let us look - there might be a shared empty state. Also 1x for Community.

### 1x No shared notes (empty state)
Spec: Illustration, "No shared notes yet. Be the first!", body, "Share your notes" primary button. Matches Community empty state concept.

## Identified Gaps (specific details)

1. **Welcome illustration (1c)**: Spec has more layered decorative dots/shapes (positioned absolutely) - our WelcomeIllustration is simpler but captures the core (paper card rotated, event card SUN 11, check badge). Visually acceptable but not pixel-perfect to spec's exact composition. Could be refined to match the layered look.

2. **Guest Profile avatar size (1e)**: Spec shows 72dp avatar; current ProfileHeader in GuestProfile uses 56dp. Also the warning dot position relative to 72dp.

3. **Guest Profile summary card style (1e)**: Spec labels it as "tonal" card with specific padding; our ProfileSummaryCard uses Surface with surfaceContainer color - tonally similar. Also in 1e the rows have per-row padding/height as shown.

4. **Connect card (1e)**: Filled-primary with white text on primary background. Our ConnectAnnexCard uses BubtCardStyle.Filled with primaryContainer/onPrimaryContainer? Check: in components, Filled uses primaryContainer/onPrimaryContainer typically. But spec says "filled-primary surface" - CTA should stand out. Current implementation uses BubtCardStyle.Filled; in our theme filled cards use primaryContainer. But spec shows solid primary background (#2563EB) for the Connect card in Guest context. Compare: ConnectAnnexCard does BubtCard(modifier = modifier, style = BubtCardStyle.Filled) and puts content with onPrimaryContainer colors. That gives primaryContainer background - close to primary. Maybe should be solid primary for max emphasis as spec shows.

5. **Profile identity card (1u)**: Spec shows outlined card with avatar 64dp, name, ID, tags. Our ProfileIdentity uses BubtCard style Outlined and avatar 48dp? Let us check - in ProfileScreen/ProfileIdentity: avatar size 48dp (seen in context), initials. Spec says 64dp. Also tags styling.

6. **Profile NOTIFICATIONS card (1u)**: Outlined card with rows. Spec shows specific heights (54dp rows) and dividers. Our NotificationSettings uses Surface with outline border and Row items - structure matches.

7. **Empty states (1w, 1x)**: We have text fallbacks but not the illustrated empty states as specified. Spec shows custom illustrations for empty states. Should add EmptyState composables matching spec illustrations.

8. **Scanner steps 3/4 polish**: Capture step (1q) - camera preview frame, grid, capture button (large), upload from gallery, thumbnails. Review step (1r) - page thumbnails with drag handle, action icons (crop/rotate/filter/delete), filename field. We have implementations; need to verify against exact layouts.

9. **Results panel (1j)**: Spec shows solid primary panel with specific padding and split. Our SgpaCgpaPanel uses primary color - matches. Delta icon/color logic: +0.14 uses trending_up and primary/onPrimary colors.

10. **Minor spacing/font weights**: Need to match exact padding (20dp screen, 16dp card) - we have BubtSpacing.Screen=20.dp, CardPadding=16.dp. Typography weights - Poppins weights used.

11. **Academics tabs order**: Spec shows Routine/Exams/Results/Attendance/Calendar in 1h-1l; 1m shows Attendance/Calendar/Courses as tabs in Courses view? Wait look up: 1m extracted shows tabs line "Attendance � Calendar � Courses" (selected Courses). Our AcademicsTab enum has Routine, Exams, Results, Attendance, Calendar, Courses - 6 tabs total. Spec 1h shows tabs Routine/Exams/Results/Attendance/Calendar (5 tabs). 1m is under Academics hub showing Courses tab selected - so total 6 tabs as implemented. Correct.

12. **1v Timetable**: Spec has 1v section. Do we need a separate Timetable view? The Routine tab (1h) is the main routine. 1v might be an alternate/extended timetable view - should compare. Let us look up 1v content more.
