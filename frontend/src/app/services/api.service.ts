import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export type CreateTaskPayload =
{
    name: string; description: string; dueDate: string; priority: 'LOW'|'MEDIUM'|'HIGH'
};

@Injectable()
export class ApiService
{
  private base = '/api';
  constructor(private http: HttpClient){}

  /* POST CONNEXION UTILISATEUR */
  login(email:string, password:string)
  {
      return this.http.post<any>(`${this.base}/auth/login`, {email,password}).toPromise();
  }

  /* POST ENRENGISTREMENT UTILISATEUR */
  register(username:string, email:string, password:string)
  {
      return this.http.post<any>(`${this.base}/auth/register`, {username,email,password}).toPromise();
  }

  /* GET LES PROJETS */
  projects()
  {
      return this.http.get<any[]>(`${this.base}/projects`).toPromise();
  }

  /* POST CREATION D'UN PROJET */
  createProject(userId:number, name:string, description:string, startDate:string)
  {
      return this.http.post<any>(`${this.base}/projects`, {name,description,startDate}).toPromise();
  }

  /* GET INFOS D'UN PROJET POUR UN UTILISATEUR */
  me(projectId:number)
  {
      return this.http.get<any>(`${this.base}/projects/${projectId}/me`).toPromise();
  }

  /* GET LES MEMBRES D'UN PROJET */
  members(projectId:number)
  {
      return this.http.get<any[]>(`${this.base}/projects/${projectId}/members`).toPromise();
  }

  /* POST INVITE UN UTILISATEUR SUR UN PROJET */
  invite(projectId:number, email:string)
  {
      return this.http.post<any>(`${this.base}/projects/${projectId}/invite`, { email }).toPromise();
  }

  /* POST MODIFIE LE ROLE D'UN UTILISATEUR POUR UN PROJET */
  setRole(projectId:number, userId:number, role:'ADMIN'|'MEMBER'|'OBSERVER')
  {
      return this.http.post<any>(`${this.base}/projects/${projectId}/roles`, { userId, role }).toPromise();
  }

  /* GET LES DETAILS D'UNE TACHE POUR UN PROJET */
  task(userId:number, taskId:number)
  {
      return this.http.get<any>(`${this.base}/tasks/${taskId}`).toPromise();
  }

  /* GET TOUTES LES INFOS DE BASE DES TACHES D'UN PROJET */
  tasks(userId:number, projectId:number, status?:string)
  {
      const q = status?`?status=${status}`:'';
      return this.http.get<any[]>(`${this.base}/projects/${projectId}/tasks${q}`).toPromise();
  }

  /* POST CREE UNE TACHE POUR UN PROJET */
  createTask(userId:number, projectId:number, payload:CreateTaskPayload)
  {
      return this.http.post<any>(`${this.base}/projects/${projectId}/tasks`, payload).toPromise();
  }

  /* PUT MODIFIE UNE TACHE POUR UN PROJET */
  updateTask(userId:number, taskId:number, payload:any)
  {
      return this.http.put<any>(`${this.base}/tasks/${taskId}`, payload).toPromise();
  }

  /* POST ASSIGNE UN UTILISATEUR A LA TACHE */
  assignTask(taskId:number, userId:number)
  {
      return this.http.post<any>(`${this.base}/tasks/${taskId}/assign`, { userId }).toPromise();
  }

  /* GET L'HISTORIQUE D'UNE TACHE */
  taskHistory(taskId:number)
  {
      return this.http.get<any[]>(`${this.base}/tasks/${taskId}/history`).toPromise();
  }

  /* POST NOUVELLE LIGNE DANS L'HISTORIQUE DE LA TACHE */
  async addTaskHistory(taskId: number, body:
      {
        authorId: number;
        action: 'CREATED' | 'UPDATED' | 'STATUS' | 'ASSIGNEE';
        field?: 'name' | 'description' | 'dueDate' | 'completedAt' | 'priority' | 'status' | 'assignedTo';
        oldValue?: string|null;
        newValue?: string|null;
      })
  {
      return this.http.post(`${this.base}/tasks/${taskId}/history`, body).toPromise();
  }
}