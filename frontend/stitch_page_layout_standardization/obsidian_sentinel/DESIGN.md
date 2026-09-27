---
name: Obsidian Sentinel
colors:
  surface: '#141A21'
  surface-dim: '#101419'
  surface-bright: '#36393f'
  surface-container-lowest: '#0a0e13'
  surface-container-low: '#181c21'
  surface-container: '#1c2025'
  surface-container-high: '#262a30'
  surface-container-highest: '#31353b'
  on-surface: '#e0e2ea'
  on-surface-variant: '#bbc9cd'
  inverse-surface: '#e0e2ea'
  inverse-on-surface: '#2d3136'
  outline: '#859397'
  outline-variant: '#3c494c'
  surface-tint: '#2fd9f4'
  primary: '#8aebff'
  on-primary: '#00363e'
  primary-container: '#22d3ee'
  on-primary-container: '#005763'
  inverse-primary: '#006877'
  secondary: '#bdc7d4'
  on-secondary: '#28313b'
  secondary-container: '#404a55'
  on-secondary-container: '#afb9c6'
  tertiary: '#d0ddff'
  on-tertiary: '#002e6a'
  tertiary-container: '#a5c1ff'
  on-tertiary-container: '#004ba5'
  error: '#EF4444'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#a2eeff'
  primary-fixed-dim: '#2fd9f4'
  on-primary-fixed: '#001f25'
  on-primary-fixed-variant: '#004e5a'
  secondary-fixed: '#d9e3f1'
  secondary-fixed-dim: '#bdc7d4'
  on-secondary-fixed: '#131c26'
  on-secondary-fixed-variant: '#3e4852'
  tertiary-fixed: '#d8e2ff'
  tertiary-fixed-dim: '#adc6ff'
  on-tertiary-fixed: '#001a42'
  on-tertiary-fixed-variant: '#004395'
  background: '#101419'
  on-background: '#e0e2ea'
  surface-variant: '#31353b'
  hover: '#1B232C'
  border: '#2A333D'
  text-primary: '#E6E9EC'
  success: '#22C55E'
  warning: '#F59E0B'
  info: '#6B7280'
  high-severity: '#F97316'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  data-mono:
    fontFamily: JetBrains Mono
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: -0.02em
  label-caps:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  sidebar-width: 240px
  topbar-height: 64px
  container-padding: 24px
  element-gap: 16px
  inner-padding: 12px
---

## Brand & Style
The brand personality is **vigilant, technical, and authoritative**. As a security monitoring system, the UI must evoke a sense of absolute control and situational awareness. The target audience consists of Security Operations Center (SOC) analysts and system administrators who require high legibility and low eye strain during extended monitoring sessions.

The design style is **Corporate / Modern** with a **Minimalist** edge. It prioritizes data density and functional hierarchy over decorative elements. By utilizing a deep, dark palette with surgical applications of high-contrast "Teal/Cyan" accents, the system differentiates critical alerts from routine telemetry. The interface remains flat to ensure speed of recognition, using sharp typography and subtle borders rather than shadows to define structure.

## Colors
This design system utilizes a **high-contrast dark mode** strategy. The background (`#0B0F14`) provides a neutral canvas that recedes, allowing the surface containers (`#141A21`) to define the layout structure. 

**Functional Color Strategy:**
- **Primary (Accent):** Teal/Cyan is used exclusively for interactive elements, primary actions, and focus states.
- **Severity Levels:** A dedicated 5-tier color scale is mapped to security events:
  - **Level 1 (Info):** Gray
  - **Level 2 (Low):** Blue
  - **Level 3 (Medium):** Yellow
  - **Level 4 (High):** Orange
  - **Level 5 (Critical):** Red
- **Status Indicators:** Success and Online states share the same green hex to maintain a unified "Positive" semantic meaning.

## Typography
The system employs a dual-font strategy to separate UI navigation from technical data analysis. 

- **UI Sans-Serif (Inter):** Used for all navigational elements, headers, and standard body text. It is chosen for its exceptional legibility in dark mode and high x-height.
- **Technical Monospace (JetBrains Mono):** This is the "Data Layer" font. It must be used for IP addresses, log entries, raw JSON payloads, and API keys. The slight reduction in font size for mono (13px) allows for higher data density in tables without sacrificing readability.

All headers should utilize a semi-bold weight (600) to stand out against the dark backgrounds. Use `label-caps` for table headers and small categories to provide visual anchors.

## Layout & Spacing
The layout follows a **Fixed Sidebar + Fluid Content** model. 

- **The Shell:** A fixed 240px sidebar on the left and a 64px top bar. The main content area occupies the remaining viewport.
- **Grid & Alignment:** The system uses a standard 8px spacing scale. Large cards within the main content area are separated by a 16px (`element-gap`).
- **Responsive Behavior:** 
  - **Desktop:** Full sidebar visibility.
  - **Tablet:** Sidebar collapses to an icon-only rail (64px).
  - **Mobile:** Sidebar becomes a hidden drawer; top bar retains breadcrumbs and user menu. 
- **Data Density:** In lists and tables, vertical padding is reduced to 8px per row to maximize the amount of information visible on a single screen.

## Elevation & Depth
This design system rejects traditional shadows and blurred depths in favor of **Tonal Layers and Low-Contrast Outlines**.

Hierarchy is established through color luminosity:
1. **Level 0 (Background):** `#0B0F14` - The deepest layer.
2. **Level 1 (Card/Surface):** `#141A21` - Used for primary content containers.
3. **Level 2 (Hover/Overlay):** `#1B232C` - Used for tooltips, dropdown menus, and hover states.

**Borders:** Every card and UI boundary must use a 1px solid border (`#2A333D`). This "Ghost Border" approach provides structural clarity in dark mode where shadows often become muddy or invisible. No backdrop blurs are used to maintain maximum performance for real-time data rendering.

## Shapes
A consistent **8px (rounded-lg)** radius is applied to all primary containers (cards, modals, and input fields). 

- **Buttons & Small Components:** Use the default `roundedness` of 0.5rem (8px). 
- **Status Badges:** Use a "Pill-shape" (full radius) to distinguish them from interactive buttons.
- **Sidebar Selection:** The active state in the sidebar uses a 0px radius on the left edge (aligned with the screen border) and a 4px radius on the inner edge to create a "tab" feel.

## Components
- **Buttons:** Primary buttons use the Accent color (`#22D3EE`) with black text for maximum contrast. Secondary buttons are outlined using the Border color with Text-Primary labels.
- **Status Dots:** 8px circles used to indicate agent status. Online = Success Green; Offline = Info Gray.
- **Data Tables:** Headers are `#8A94A0` in `label-caps`. Rows feature a subtle hover background change to `#1B232C`. Column dividers are avoided; use horizontal rules only.
- **Cards:** Flat surfaces with 1px borders. No shadows. Padding should be a consistent 24px.
- **Input Fields:** Background matches the page background (`#0B0F14`) to "recede" into the surface container. Borders highlight to the Accent color on focus.
- **Badges:** Small, high-contrast labels for "Severity" or "Event Type." Backgrounds for badges should be semi-transparent versions of their functional color (e.g., Red at 15% opacity) with a solid text label to ensure the background doesn't overwhelm the text.
- **Drawers:** Slide from the right to show "Event Details." They should use the Surface color (`#141A21`) and cover 40% of the screen width.