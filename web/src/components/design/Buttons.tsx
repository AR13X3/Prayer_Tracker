import type { ButtonHTMLAttributes } from 'react'

type BaseProps = ButtonHTMLAttributes<HTMLButtonElement> & { fullWidth?: boolean }

/** Black (or coral, when accent) pill button with a press-scale on click. */
export function PillButton({
  accent = false,
  fullWidth = false,
  className = '',
  children,
  ...rest
}: BaseProps & { accent?: boolean }) {
  const bg = accent ? 'bg-coral text-white' : 'bg-ink text-white'
  return (
    <button
      {...rest}
      className={`${bg} ${fullWidth ? 'w-full' : ''} rounded-full px-6 py-3.5 font-semibold
        transition-transform active:scale-[0.97] disabled:opacity-35 disabled:pointer-events-none
        ${className}`}
    >
      {children}
    </button>
  )
}

/** Outlined pill for secondary actions. */
export function OutlinePill({ fullWidth = false, className = '', children, ...rest }: BaseProps) {
  return (
    <button
      {...rest}
      className={`${fullWidth ? 'w-full' : ''} rounded-full border-[1.5px] border-track px-5 py-3
        font-semibold text-ink transition-colors hover:bg-track/60 disabled:opacity-40
        disabled:pointer-events-none ${className}`}
    >
      {children}
    </button>
  )
}

/** Small selectable chip (prayer status / jamaah / toggles). */
export function StatusPill({
  text,
  selected,
  onClick,
  disabled = false,
}: {
  text: string
  selected: boolean
  onClick: () => void
  disabled?: boolean
}) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`rounded-full px-3.5 py-2 text-sm font-medium transition-colors
        disabled:opacity-50 disabled:pointer-events-none
        ${selected ? 'bg-coral text-white' : 'bg-track text-muted'}`}
    >
      {text}
    </button>
  )
}
