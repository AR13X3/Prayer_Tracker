import { useEffect, useRef, useState, type ReactNode } from 'react'

/** Lightweight click-to-open menu — closes on outside click or Escape. */
export function Menu({
  trigger,
  children,
  align = 'right',
}: {
  trigger: (open: () => void) => ReactNode
  children: (close: () => void) => ReactNode
  align?: 'left' | 'right'
}) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    function onDown(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    function onKey(e: KeyboardEvent) {
      if (e.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', onDown)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onDown)
      document.removeEventListener('keydown', onKey)
    }
  }, [open])

  return (
    <div className="relative" ref={ref}>
      {trigger(() => setOpen(true))}
      {open && (
        <div
          className={`absolute z-20 mt-2 min-w-40 overflow-hidden rounded-2xl bg-card py-1.5
            shadow-[0_8px_30px_rgba(0,0,0,0.12)] ${align === 'right' ? 'right-0' : 'left-0'}`}
        >
          {children(() => setOpen(false))}
        </div>
      )}
    </div>
  )
}

export function MenuItem({ onClick, children }: { onClick: () => void; children: ReactNode }) {
  return (
    <button onClick={onClick} className="block w-full px-4 py-2.5 text-left text-sm text-ink hover:bg-warm">
      {children}
    </button>
  )
}

export function MenuDivider() {
  return <div className="my-1 h-px bg-track" />
}
