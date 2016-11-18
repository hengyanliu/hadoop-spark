package mapreduce.example;

import hadoopmap.MyWordCount;

import java.io.IOException;

import junit.framework.Test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class DataMapreduce {

	// Mapper �� key ,value �ֻ�����,�Զ�����������
	static class DataMapper extends
			Mapper<LongWritable, Text, Text, DataWritable> {

		private Text mapOutputKey = new Text();
		private DataWritable dataWritable = new DataWritable();

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// ��ȡ����
			String lineValue = value.toString();
			// ���ݷָ�
			String[] strs = lineValue.split("\t");
			// �õ�����
			String phoneNum = strs[1];
			int upPackNum = Integer.valueOf(strs[6]);
			int downPackNum = Integer.valueOf(strs[7]);
			int upPayLoad = Integer.valueOf(strs[8]);
			int downPayLoad = Integer.valueOf(strs[9]);

			// ����map�����key/value
			mapOutputKey.set(phoneNum);
			dataWritable.set(upPackNum, upPayLoad, downPackNum, downPayLoad);

			// ����map���
			context.write(mapOutputKey, dataWritable);
		};
	}

	// Reduce�� �ֻ�����,�Զ�����������,ͳ�ƺõ��ֻ�����,�Զ�����������
	static class DataReducer extends
			Reducer<Text, DataWritable, Text, DataWritable> {

		private DataWritable dataWritable = new DataWritable();

		public void reduce(Text key, Iterable<DataWritable> values,
				Context context) throws IOException, InterruptedException {
			int upPackNum = 0;
			int downPackNum = 0;
			int upPayLoad = 0;
			int downPayLoad = 0;

			// ѭ��
			for (DataWritable data : values) {
				upPackNum += data.getUpPackNum();
				downPackNum += data.getDownPackNum();
				upPayLoad += data.getUpPayLoad();
				downPayLoad += data.getDownPayLoad();
			}
			// ���������dataWritable
			dataWritable.set(upPackNum, upPayLoad, downPackNum, downPayLoad);

			// ����reduce/job�����
			context.write(key, dataWritable);

		};
	}

	// ����
	public int run(String[] args) throws Exception {
		// ��ȡ����
		Configuration conf = new Configuration();

		// ����Job,�������ú�Job����
		Job job = new Job(conf, DataMapreduce.class.getSimpleName());

		// 1������Job���е���
		job.setJarByClass(DataMapreduce.class);

		// 2:����Mapper��Reducer��
		job.setMapperClass(DataMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(DataWritable.class);
		
		job.setReducerClass(DataReducer.class);
		job.setOutputKeyClass(Test.class);
		job.setOutputValueClass(DataWritable.class);
	

		// 3�����������ļ���Ŀ¼��������ļ���Ŀ¼
        Path intputDir = new Path(args[0]);
        FileInputFormat.addInputPath(job, intputDir);
        
        Path outputDir = new Path(args[1]);
        FileOutputFormat.setOutputPath(job, outputDir);
        
		
		// 4������������key��value����

		// 5���ύJob���ȴ����н�������ڿͻ�����ʾ������Ϣ
		boolean isSuccess = job.waitForCompletion(true);
		return isSuccess ? 0 : 1;

	}

	// ��
	public static void main(String[] args) throws Exception {
		// set args
		args = new String[] {
				"hdfs://hadoop-master.dragon.org:9000/opt/wc/input/",
				"hdfs://hadoop-master.dragon.org:9000/opt/wc/output/", };

		// run job
		int status = new DataMapreduce().run(args);
		// exit
		System.exit(status);
	}

}
