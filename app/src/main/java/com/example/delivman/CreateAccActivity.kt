package com.example.delivman

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class CreateAccActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_acc)


        val userLogin: EditText = findViewById(R.id.user_login)
        val userEmail: EditText = findViewById(R.id.user_email)
        val userPass: EditText = findViewById(R.id.user_pass)
        val button: Button = findViewById(R.id.button_reg)
        val linkToAuth: TextView = findViewById(R.id.link_to_auth)
        linkToAuth.setOnClickListener{
            val intent = Intent(this,AuthActivity::class.java)//запустит мэйнактивити тоесть регистрацию
            startActivity(intent)
        }

        button.setOnClickListener{
            val login = userLogin.text.toString().trim()
            val email = userEmail.text.toString().trim()
            val password = userPass.text.toString().trim()
            if(login==""|| email == "" || password == "")
                Toast.makeText(this,"не все поля заполнены", Toast.LENGTH_SHORT).show()
            else{
                val user = User(login,email,password)

                val db = DbHelper(this, null)
                db.addUser(user)
                Toast.makeText(this,"Пользователь $login добавлен", Toast.LENGTH_SHORT).show()
                userLogin.text.clear()
                userEmail.text.clear()
                userPass.text.clear()
            }
        }
    }
}