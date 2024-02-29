package com.example.delivman

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast



class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)


        val userLogin: EditText = findViewById(R.id.user_login_auth)
        val userPass: EditText = findViewById(R.id.user_pass_auth)
        val button: Button = findViewById(R.id.button_auth)
        button.setOnClickListener{
            val login = userLogin.text.toString().trim()
            val password = userPass.text.toString().trim()
            if(login==""|| password == "")
                Toast.makeText(this,"не все поля заполнены", Toast.LENGTH_SHORT).show()
            else{
                val db = DbHelper(this, null)
                val isAuth = db.getUser(login,password)

                if(isAuth){
                    Toast.makeText(this,"Пользователь $login авторизован", Toast.LENGTH_SHORT).show()
                    userLogin.text.clear()
                    userPass.text.clear()

                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    //здесь описать события после
                }
                else
                    Toast.makeText(this,"Пользователь $login не авторизован", Toast.LENGTH_SHORT).show()
                Toast.makeText(this,"Пользователь $login добавлен", Toast.LENGTH_SHORT).show()

            }
        }

        val linkToReg:TextView = findViewById(R.id.link_to_reg)
        linkToReg.setOnClickListener{
            val intent1 = Intent(this,CreateAccActivity::class.java)//запустит мэйнактивити тоесть регистрацию
            startActivity(intent1)
        }
    }
}