import type { ReactNode } from 'react'

export function SoftCard({
  children,
  className = '',
  padding = 'p-5',
}: {
  children: ReactNode
  className?: string
  padding?: string
}) {
  return <div className={`bg-card rounded-3xl ${padding} ${className}`}>{children}</div>
}

export function SectionLabel({ text, className = '' }: { text: string; className?: string }) {
  return (
    <div className={`text-[11px] font-semibold uppercase tracking-wider text-muted ${className}`}>
      {text}
    </div>
  )
}

export function ScreenHeader({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div>
      <h1 className="text-2xl font-bold text-ink">{title}</h1>
      {subtitle && <p className="text-sm text-muted mt-0.5">{subtitle}</p>}
    </div>
  )
}
