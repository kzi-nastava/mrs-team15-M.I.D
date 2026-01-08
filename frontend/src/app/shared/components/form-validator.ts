export class FromValidator {
  private emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  private phonePattern = /^(\+381|0)[0-9]{9,10}$/;
  private namePattern = /^[A-ZČĆŠĐŽ][a-zčćšđž]+$/;
  private addressPattern = /^[A-Za-zČĆŠĐŽčćšđž0-9\s.,\/-]{5,}$/;

  firstNameError(value: string): string | null {
    if (!value) return 'First name is required';
    if (!this.namePattern.test(value)) return 'Only letters, first letter uppercase';
    return null;
  }

  lastNameError(value: string): string | null {
    if (!value) return 'Last name is required';
    if (!this.namePattern.test(value)) return 'Only letters, first letter uppercase';
    return null;
  }

  emailError(value: string): string | null {
    if (!value) return 'Email is required';
    if (!this.emailPattern.test(value)) return 'Email format is invalid';
    return null;
  }

  passwordError(value: string): string | null {
    if (!value) return 'Password is required';
    if (value.length < 6) return 'Password must be at least 6 characters';
    return null;
  }

  confirmPasswordError(password: string, confirm: string): string | null {
    if (!confirm) return 'Confirm password is required';
    if (password !== confirm) return 'Passwords must match';
    return null;
  }

  phoneError(value: string): string | null {
    if (!value) return 'Phone number is required';
    if (!this.phonePattern.test(value)) return 'Phone number is not valid';
    return null;
  }

  addressError(value: string): string | null {
    if (!value) return 'Address is required';
    if (!this.addressPattern.test(value)) return 'Address must be at least 5 characters';
    return null;
  }

  reasonError(value: string) : string | null {
    if(!value) return 'Reason is required';
    if(value.trim() === '') return  'Reason is required';
    return null;
  }

  timeError(value: string): string | null {
    if(!value) return 'Time is required';
    if(value.trim() === '') return  'Time is required';
    return null;
  }
  
  dateError(value: string): string | null {
    if(!value) return 'Date is required';
    if(value.trim() === '') return  'Date is required';
    return null;
  }


}