export class FromValidator{
    private emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    private phonePattern = /^(\+381|0)[0-9]{9,10}$/;
    
    isFieldEmpty(field: string): boolean {
        return !field || field.trim() === '';
    }
    
    isLengthValid(value : string, length : number) : boolean{
        if (!value || value.length < length || value.trim() === '') return false;
        return true;
    }
    
    isEmailValid(email: string): boolean {
        if (!email) return false;
        return this.emailPattern.test(email);
    }

    isPasswordValid(password : string){
        return this.isLengthValid(password, 6);
    } 
    
    isPhoneValid(phone: string): boolean {
        if (!phone) return false;
        return this.phonePattern.test(phone);
    }

    areMatch(firstValue : string, secondValue : string) : boolean{
        return firstValue === secondValue
    }
}