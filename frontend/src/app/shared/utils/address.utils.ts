/**
 * Formats a full address string to display only street number, street name, and city.
 *
 * @param fullAddress - The complete address string (e.g., "4, Cika Stevina, Нова Детелинара, Detelinara, МЗ Детелинара, Novi Sad, City of Novi Sad, South Backa Administrative District, Vojvodina, 21137, Serbia")
 * @returns A shortened address string (e.g., "4, Cika Stevina, Novi Sad")
 */
export function formatAddress(fullAddress: string): string {
  // Split the address by commas
  const parts = fullAddress.split(',').map(p => p.trim());

  if (parts.length < 3) {
    return fullAddress; // Return as-is if format is unexpected
  }

  // Extract: number, street name, and city
  // Typically: "4, Cika Stevina, ..., Novi Sad, ..."
  const number = parts[0];

  // Check if number only contains digits
  const isValidNumber = /^\d+$/.test(number);
  if (!isValidNumber) {
    const street = parts[0];
  }

  const street = parts[1];
  const city = parts[parts.length - 6];

  return `${number}, ${street}, ${city}`;
}
