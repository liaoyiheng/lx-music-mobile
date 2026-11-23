/**
 * 返回打乱顺序后的新数组（Fisher–Yates），不修改原数组
 */
export const shuffleList = <T>(list: T[]): T[] => {
  const arr = [...list]
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    const tmp = arr[i]
    arr[i] = arr[j]
    arr[j] = tmp
  }
  return arr
}
