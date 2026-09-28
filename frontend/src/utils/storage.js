// localStorage can be unavailable (private mode, blocked storage) — never let that break the app.
export function load(key, fallback) {
  try {
    const v = localStorage.getItem(key)
    return v == null ? fallback : JSON.parse(v)
  } catch {
    return fallback
  }
}

export function save(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch {
    /* ignore */
  }
}

export function remove(key) {
  try {
    localStorage.removeItem(key)
  } catch {
    /* ignore */
  }
}

const RECENT_KEY = 'fk_recently_viewed'

export function addRecentlyViewed(id) {
  const ids = load(RECENT_KEY, []).filter((x) => x !== id)
  ids.unshift(id)
  save(RECENT_KEY, ids.slice(0, 12))
}

export function recentlyViewed() {
  return load(RECENT_KEY, [])
}
