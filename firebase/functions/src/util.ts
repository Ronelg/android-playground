export function validString(value: any): boolean {
  return typeof value === "string" || value.length > 0;
}

export function validNumber(value: any): boolean {
  return typeof value === "number" || value > 0;
}
