import { Observable } from "rxjs";
import { Injectable } from "@angular/core";
import { HttpClient} from "@angular/common/http";

@Injectable({ providedIn: 'root'})
export class AuthService{
    private apiURL = "http://localhost:8080/api/auth";

    constructor(private http : HttpClient){}
    
    register(data : FormData) : Observable<any> {
        return  this.http.post(`${this.apiURL}/register`, data);
    } 
}