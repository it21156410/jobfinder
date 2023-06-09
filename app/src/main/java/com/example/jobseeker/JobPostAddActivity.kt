package com.example.jobseeker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jobseeker.databinding.ActivityJobPostAddBinding
import com.example.jobseeker.model.JobPostModel
import com.example.jobseeker.utils.Config
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JobPostAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobPostAddBinding
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobPostAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val goBackButton = findViewById<Button>(R.id.goBackAddJobPost)

        goBackButton.setOnClickListener {
            finish()
        }

        val isEdit = intent.getBooleanExtra("isEdit", false)
        var jobPostId: String? = null
        if (isEdit) {
            jobPostId = intent.getStringExtra("jobPostId")
            if (jobPostId != null) {
                database.child("job_posts").child(jobPostId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val jobPost = snapshot.getValue(JobPostModel::class.java)
                        if (jobPost != null) {
                            binding.editTextPosition.setText(jobPost.position)
                            binding.editTextQualification.setText(jobPost.qualification)
                            binding.editTextCompanyName.setText(jobPost.companyName)
                            binding.editTextStartDate.setText(jobPost.startDate)
                            binding.editTextEndDate.setText(jobPost.endDate)
                            binding.editTextJobDescription.setText(jobPost.jobDescription)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@JobPostAddActivity, "Failed to retrieve job post data", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        binding.jobPostSubmitButton.setOnClickListener {
            if (validateData()) {
                if (isEdit) {
                    updateData(jobPostId!!)
                } else {
                    storeData()
                }
            }
        }
    }

    private fun validateData(): Boolean {
        val position = binding.editTextPosition.text.toString()
        val qualification = binding.editTextQualification.text.toString()
        val companyName = binding.editTextCompanyName.text.toString()
        val startDate = binding.editTextStartDate.text.toString()
        val endDate = binding.editTextEndDate.text.toString()
        val jobDescription = binding.editTextJobDescription.text.toString()

        if (position.isBlank() || qualification.isBlank() || companyName.isBlank() ||
            startDate.isBlank() || endDate.isBlank() || jobDescription.isBlank()
        ) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return false
        } else if (position.length > 20) {
            Toast.makeText(this, "Job position cannot be longer than 20 characters", Toast.LENGTH_LONG).show()
            return false
        } else if (companyName.length > 28) {
            Toast.makeText(this, "Job company name cannot be longer than 28 characters", Toast.LENGTH_LONG).show()
            return false
        } else if (jobDescription.length > 180) {
            Toast.makeText(this, "Job description cannot be longer than 180 characters", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun storeData() {
        val jobPost = JobPostModel(
            position = binding.editTextPosition.text.toString(),
            qualification = binding.editTextQualification.text.toString(),
            companyName = binding.editTextCompanyName.text.toString(),
            startDate = binding.editTextStartDate.text.toString(),
            endDate = binding.editTextEndDate.text.toString(),
            jobDescription = binding.editTextJobDescription.text.toString()
        )

        Config.showDialog(this)
        val jobPostId = database.child("job_posts").push().key
        if (jobPostId != null) {
            database.child("job_posts").child(jobPostId).setValue(jobPost)
                .addOnSuccessListener {
                    Config.hideDialog()
                    Toast.makeText(this, "Job post added successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, JobPostManageActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Config.hideDialog()
                    Toast.makeText(this, "Failed to add job post", Toast.LENGTH_SHORT).show()
                }
        } else {
            Config.hideDialog()
            Toast.makeText(this, "Failed to generate job post id", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateData(jobPostId: String?) {
        val jobPost = JobPostModel(
            position = binding.editTextPosition.text.toString(),
            qualification = binding.editTextQualification.text.toString(),
            companyName = binding.editTextCompanyName.text.toString(),
            startDate = binding.editTextStartDate.text.toString(),
            endDate = binding.editTextEndDate.text.toString(),
            jobDescription = binding.editTextJobDescription.text.toString()
        )

        Config.showDialog(this)
        if (jobPostId != null) {
            database.child("job_posts").child(jobPostId).setValue(jobPost)
                .addOnSuccessListener {
                    Config.hideDialog()
                    Toast.makeText(this, "Job post updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, JobPostManageActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Config.hideDialog()
                    Toast.makeText(this, "Failed to update job post", Toast.LENGTH_SHORT).show()
                }
        } else {
            Config.hideDialog()
            Toast.makeText(this, "Failed to update job post", Toast.LENGTH_SHORT).show()
        }
    }

}
